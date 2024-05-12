package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.TeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-05-05
 */
public interface CourseTeacherService extends IService<CourseTeacher> {

    CourseTeacher addTeacher(CourseTeacher courseTeacher);

    List<CourseTeacher> getList(Long id);

    void deleteTeacher(Long courseId, Long teacherId);
}
