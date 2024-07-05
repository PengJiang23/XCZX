package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * 
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MediaFileService currentProxy;

    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Value("${minio.bucket.videofiles}")
    private String bucket_videofiles;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();


        queryWrapper.like(StringUtils.isNotEmpty(queryMediaParamsDto.getFilename()), MediaFiles::getFilename, queryMediaParamsDto.getFilename())
                .eq(StringUtils.isNotEmpty(queryMediaParamsDto.getFileType()), MediaFiles::getFileType, queryMediaParamsDto.getFileType());

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }


    /**
     * 上传minio
     * 获取mimetype，没有则获取通用字节流
     *
     * @param extension
     * @return
     */
    private String getMimeType(String extension) {
        if (extension == null) {
            extension = "";
        }
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 获取上传到minio中指定路径，以日期作为目录
     *
     * @return
     */
    private String getDefaultFolderPath() {
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd");
        return simpleFormatter.format(new Date()).replace("-", "/");
    }

    /**
     * 获取上传文件的md5
     *
     * @param file
     * @return
     */
    private String getFileMd5(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            String s = DigestUtils.md5Hex(fileInputStream);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addMediaFileToMinoIO(String localFilePath, String objectName, String mimeType, String bucket) {
        // 构造objectName
        // 获取mimetype
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)//上传后的对象名
                    .filename(localFilePath)
                    .contentType(mimeType)//默认根据扩展名确定文件内容类型，也可以指定
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.info("文件上传成功");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("文件上传失败" + "bucket" + bucket + "objectName" + objectName + "localFilePath" + localFilePath);
            XueChengPlusException.cast("上传文件到minio失败");
        }
        return false;
    }

    @Override
    public MediaFiles getFileById(String mediaId) {
      return   mediaFilesMapper.selectById(mediaId);
    }


    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");

            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败，{}", mediaFiles.toString());
                XueChengPlusException.cast("保存文件信息到数据库失败");
            }
            log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());
        }

        addWaitingTask(mediaFiles);

        return mediaFiles;
    }


    private void addWaitingTask(MediaFiles mediaFiles) {

        String filename = mediaFiles.getFilename();
        String mimeType = getMimeType(filename.substring(filename.lastIndexOf(".")));
        if (mimeType.equals("video/x-msvideo")) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcessMapper.insert(mediaProcess);
        }
    }




    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamDto, String localFilePath, String objectName) {

        // 获取文件拓展名和mimetype  每个上传文件都需要
        // 构造上传的minio的对象
        // 构造上传文件保存信息，并插入到数据库中

        File file = new File(localFilePath);
        if (!file.exists()) {
            XueChengPlusException.cast("文件不存在");
        }
        String filename = uploadFileParamDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        String mimeType = getMimeType(extension);
        String fileMd5 = getFileMd5(file);

        if(StringUtils.isEmpty(objectName)){
            String defaultFolderPath = getDefaultFolderPath();
            objectName = defaultFolderPath + fileMd5 + extension;
        }

        addMediaFileToMinoIO(localFilePath, objectName, mimeType, bucket_files);
        uploadFileParamDto.setFileSize(file.length());
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamDto, bucket_files, objectName);
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;

    }


    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 获取文件信息，根据数据库中的文件详情去minio中查看该文件是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();

            InputStream stream = null;

            try {
                stream = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(filePath)
                        .build());
                if (stream != null) {
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
            }
        }

        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 检查分块文件是否存在
        // object的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);

        InputStream stream = null;
        try {
            stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket_videofiles)
                    .object(chunkFileFolderPath)
                    .build());
            if (stream != null) {
                return RestResponse.success(true);
            }

        } catch (Exception e) {
        }

        return RestResponse.success(false);
    }


    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + "chunk" + "/";
    }


    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {

        String mimeType = getMimeType(null);
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        String chunkFilePath = chunkFileFolderPath + chunk;

        boolean b = addMediaFileToMinoIO(localChunkFilePath, chunkFilePath, mimeType, bucket_videofiles);

        if (!b) {
            return RestResponse.success(false, "上传分块文件失败");
        }


        return RestResponse.success(true, "上传分块文件成功");
    }


    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //分块文件所在目录
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        //找到所有的分块文件
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> ComposeSource.builder().bucket(bucket_videofiles).object(chunkFileFolderPath + i).build()).collect(Collectors.toList());
        //源文件名称
        String filename = uploadFileParamsDto.getFilename();
        //扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //合并后文件的objectname
        String objectName = getFilePathByMd5(fileMd5, extension);
        //指定合并后的objectName等信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_videofiles)
                .object(objectName)//合并后的文件的objectname
                .sources(sources)//指定源文件
                .build();
        //===========合并文件============
        //报错size 1048576 must be greater than 5242880，minio默认的分块文件大小为5M
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错,bucket:{},objectName:{},错误信息:{}", bucket_videofiles, objectName, e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }

        //===========校验合并后的和源文件是否一致，视频上传才成功===========
        //先下载合并后的文件
        File file = downloadFileFromMinIO(bucket_videofiles, objectName);
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            //计算合并后文件的md5
            String mergeFile_md5 = DigestUtils.md5Hex(fileInputStream);
            //比较原始md5和合并后文件的md5
            if (!fileMd5.equals(mergeFile_md5)) {
                log.error("校验合并文件md5值不一致,原始文件:{},合并文件:{}", fileMd5, mergeFile_md5);
                return RestResponse.validfail(false, "文件校验失败");
            }
            //文件大小
            uploadFileParamsDto.setFileSize(file.length());
        } catch (Exception e) {
            return RestResponse.validfail(false, "文件校验失败");
        }

        //==============将文件信息入库============
        MediaFiles mediaFiles = currentProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_videofiles, objectName);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件入库失败");
        }
        //==========清理分块文件=========
        clearChunkFiles(chunkFileFolderPath, chunkTotal);

        return RestResponse.success(true);
    }

    /**
     * 从minio下载文件
     *
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName) {
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try {
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream, outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * 清除分块文件
     *
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal          分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath, int chunkTotal) {
        Iterable<DeleteObject> objects = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> new DeleteObject(chunkFileFolderPath + i)).collect(Collectors.toList());
        ;
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_videofiles).objects(objects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        //要想真正删除
        results.forEach(f -> {
            try {
                DeleteError deleteError = f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    /**
     * 得到合并后的文件的地址
     *
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5, String fileExt) {
        return fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }

}
