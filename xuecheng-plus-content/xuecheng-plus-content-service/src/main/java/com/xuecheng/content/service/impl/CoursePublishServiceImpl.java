package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDTO;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.model.po.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;
import rx.exceptions.Exceptions;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {


    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    @Autowired
    MqMessageService mqMessageService;

    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDTO GetCourseInfo(Long courseId) {
        CoursePreviewDTO coursePreviewDTO = new CoursePreviewDTO();
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        coursePreviewDTO.setCourseBase(courseBaseInfo);
        coursePreviewDTO.setTeachplans(teachplanTree);
        return coursePreviewDTO;
    }

    @Override
    @Transactional
    public void commitAudit(Long companyId, Long courseId) {
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null) {
            XueChengPlusException.cast("课程找不到");
        }

        String auditStatus = courseBaseInfo.getAuditStatus();
        if (auditStatus.equals("202003")) {
            XueChengPlusException.cast("课程已提交等待审核");
        }
        Long companyIdDB = courseBaseInfo.getCompanyId();
        if (!companyId.equals(companyIdDB)) {
            XueChengPlusException.cast("只能修改本公司对应的课程");
        }
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic)) {
            XueChengPlusException.cast("请上传课程图片");
        }

        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        if (teachplanTree == null || teachplanTree.size() == 0) {
            XueChengPlusException.cast("编写课程计划");
        }

        CoursePublishPre coursePublishPre = new CoursePublishPre();
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);

        //设置机构id
        coursePublishPre.setCompanyId(companyId);
        //营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);
        //计划信息
        //转json
        String teachplanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachplanTreeJson);
        //状态为已提交
        coursePublishPre.setStatus("202003");
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());
        //查询预发布表，如果有记录则更新，没有则插入
        CoursePublishPre coursePublishPreObj = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPreObj == null) {
            //插入
            coursePublishPreMapper.insert(coursePublishPre);
        } else {
            //更新
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        //更新课程基本信息表的审核状态为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus("202003");//审核状态为已提交

        courseBaseMapper.updateById(courseBase);


    }


    @Transactional
    @Override
    public void commitPublish(Long companyId, Long courseId) {

        // 查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null) {
            XueChengPlusException.cast("未提交审核");
        }
        //查询课程发布状态
        String status = coursePublishPre.getStatus();
        if (!status.equals("202004")) {
            XueChengPlusException.cast("不允许发布");
        }
        // 向课程发布表写数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        CoursePublish coursePublishObj = coursePublishMapper.selectById(courseId);
        if (coursePublishObj == null) {
            coursePublishMapper.insert(coursePublish);
        } else {
            coursePublishMapper.updateById(coursePublish);
        }

        // 向消息表写入数据
        // 使用sdk/工具类，将mq信息写入数据库
        saveCoursePublishMessage(courseId);

        // 将预发布表数据删除
        coursePublishPreMapper.deleteById(courseId);


    }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null) {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }


    @Override
    public File generateStaticHtml(Long courseId) {
        File htmlFile = null;
        //指定模板的目录
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());

            //拿到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            //指定模板的目录
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //指定编码
            configuration.setDefaultEncoding("utf-8");

            //得到模板
            Template template = configuration.getTemplate("course_template.ftl");
            //准备数据
            CoursePreviewDTO coursePreviewInfo = this.GetCourseInfo(120L);
            HashMap<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //Template template 模板, Object model 数据
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            //输入流
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            htmlFile = File.createTempFile("coursepublish", ".html");
            //输出文件
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            //使用流将html写入文件
            IOUtils.copy(inputStream, outputStream);

        } catch (Exception e) {
            log.error("页面静态化出现问题，课程id：{}", courseId, e);
            throw new RuntimeException(e);
        }
        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        try {
            MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
            String upload = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
            if (upload == null) {
                log.error("静态资源上传minio失败");
                XueChengPlusException.cast("静态资源上传minio失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            XueChengPlusException.cast("静态资源上传minio失败");
        }
    }
}
