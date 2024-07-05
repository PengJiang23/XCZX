package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.CoursePreviewDTO;
import com.xuecheng.content.model.po.CoursePublish;

import java.io.File;

/**
 * <p>
 * 课程发布 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-05-05
 */
public interface CoursePublishService extends IService<CoursePublish> {

    public CoursePreviewDTO GetCourseInfo(Long courseId);

    public void commitAudit(Long companyId, Long courseId);

    public void commitPublish(Long companyId, Long courseId);

    public File generateStaticHtml(Long courseId);

    public void uploadCourseHtml(Long courseId, File file);
}
