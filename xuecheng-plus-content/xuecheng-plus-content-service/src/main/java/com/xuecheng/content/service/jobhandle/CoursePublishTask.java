package com.xuecheng.content.service.jobhandle;


import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.CourseIndex;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CoursePreviewDTO;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;


@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract {


    @Autowired
    MessageProcessAbstract messageProcessAbstract;

    @Autowired
    CoursePublishService coursePublishService;

    @Autowired
    SearchServiceClient searchServiceClient;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    // todo mq_message表字段与实际的mqsdk字段不一致，暂时先这样，历史表插入不进去，后续再调整mq数据库字段问题 2024/6/24

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        process(shardIndex, shardTotal, "course_publish", 30, 60);

    }


    @Override
    public boolean execute(MqMessage mqMessage) {
        long courseId = Integer.parseInt(mqMessage.getBusinessKey1());
        // 课程页面静态化
        generateCourseHtml(mqMessage, courseId);
        // es写数据
        saveCourseIndex(mqMessage, courseId);

        // redis写数据
        saveCourseCache(mqMessage, courseId);

        return true;
    }

    private void saveCourseCache(MqMessage mqMessage, long courseId) {
        // 信息保存
        Long taskId = mqMessage.getId();
        //
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageThree = mqMessageService.getStageThree(taskId);
        if (stageThree > 0) {
            log.debug("信息已经保存");
            return;
        }

        // 保存第二阶段状态
        mqMessageService.completedStageTwo(taskId);
    }

    private void saveCourseIndex(MqMessage mqMessage, long courseId) {
        // 保存索引
        Long taskId = mqMessage.getId();
        //
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if (stageTwo > 0) {
            log.debug("索引已经保存");
            return;
        }

        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish,courseIndex);
        Boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XueChengPlusException.cast("远程调用添加索引失败");
        }
        // 保存第二阶段状态
        mqMessageService.completedStageTwo(taskId);
    }

    private void generateCourseHtml(MqMessage mqMessage, long courseId) {
        // 生成静态页面上传minion
        Long taskId = mqMessage.getId();
        //
        MqMessageService mqMessageService = this.getMqMessageService();
        int stageOne = mqMessageService.getStageOne(taskId);
        if (stageOne > 0) {
            log.debug("课程已经静态化");
            return;
        }
        // 页面静态化
        // 1. 获取静态页面 2. rpc+上传minio 注：这两个步骤都需要添加各自内部的异常，这样执行失败不会执行最后的更新状态逻辑（符合正常的逻辑处理）
        File file = coursePublishService.generateStaticHtml(courseId);
        // 额外异常验证
        if (file == null) {
            XueChengPlusException.cast("生成静态页面为空");
        }
        coursePublishService.uploadCourseHtml(courseId, file);
        // 保存第一阶段状态
        mqMessageService.completedStageOne(taskId);
    }


}
