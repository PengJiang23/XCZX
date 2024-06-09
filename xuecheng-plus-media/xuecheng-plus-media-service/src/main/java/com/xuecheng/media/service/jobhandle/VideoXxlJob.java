package com.xuecheng.media.service.jobhandle;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 * <p>
 * 开发步骤：
 * 1、任务开发：在Spring Bean实例中，开发Job方法；
 * 2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 * 3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 * 4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
@Slf4j
public class VideoXxlJob {

    @Autowired
    MediaProcessService mediaProcessService;

    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}")
    String ffmpeg_path;

    /**
     * 2、分片广播任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        // 获取cpu核数
        int processors = Runtime.getRuntime().availableProcessors();

        // 查询待处理任务
        List<MediaProcess> mediaProcessList = mediaProcessService.getByMediaProcess(shardTotal, shardIndex, 5);

        // 任务数量
        int size = mediaProcessList.size();
        log.debug("视频任务数{}", size);
        if (size <= 0) {
            return;
        }
        // 启动多线程
        // 1. 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            // 任务加入到线程池
            executorService.execute(() -> {
                try {
                    Long taskId = mediaProcess.getId();
                    // 开启任务
                    boolean b = mediaProcessService.startTask(taskId);
                    if (!b) {
                        log.debug("锁抢占失败");
                        return;
                    }


                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.debug("下载视频错误,taskid{},bucket,objcetname", bucket);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", mediaProcess.getFileId(), null, "下载视频失败");
                        return;
                    }


                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    // 文件MD5就是id
                    String fileId = mediaProcess.getFileId();
                    //转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";

                    File mp4File = null;
                    //转换后mp4文件的路径
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常");
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件失败");
                        return;
                    }
                    String mp4_path = mp4File.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();

                    if (!result.equals("success")) {
                        log.debug("视频转码失败，原因{}", result);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码失败");
                        return;
                    }


                    // objectName有问题，原来的.avi替换后并没有用新的.mp4更新object跟下面的url一致
                    // MP4文件url
                    String url = getFilePathByMd5(fileId, ".mp4");
                    // 上传minio
                    boolean b1 = mediaFileService.addMediaFileToMinoIO(mp4File.getAbsolutePath(), objectName.substring(0, objectName.lastIndexOf(".")) + ".mp4", "video/mp4", bucket);
                    log.error("{}", b1);
                    if (!b1) {
                        log.debug("视频转码后，文件上传失败，原因{}", bucket);
                        mediaProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码后，文件上传失败");
                        return;
                    }


                    //保存任务处理结果
                    mediaProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, "下载视频失败");
                } finally {
                    //计数器-1
                    countDownLatch.countDown();
                }

            });

        });

        // 阻塞等待所有的线程完成，为了确保线程内出现挂掉现象，需要指定最大等待时间
        countDownLatch.await(30, TimeUnit.MINUTES);

    }


    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


}
