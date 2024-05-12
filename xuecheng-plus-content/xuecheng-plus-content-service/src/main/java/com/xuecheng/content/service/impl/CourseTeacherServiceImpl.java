package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.dto.TeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 课程-教师关系表 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseTeacherServiceImpl extends ServiceImpl<CourseTeacherMapper, CourseTeacher> implements CourseTeacherService {


    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Override
    public CourseTeacher addTeacher(CourseTeacher courseTeacher) {
        if(courseTeacher.getId() == null){
            courseTeacherMapper.insert(courseTeacher);
        }else{
            courseTeacherMapper.updateById(courseTeacher);
        }
        return courseTeacherMapper.selectById(courseTeacher);
    }

    @Override
    public List<CourseTeacher> getList(Long id) {
        LambdaQueryWrapper<CourseTeacher> lambdaQueryWrapper = new LambdaQueryWrapper<CourseTeacher>();
        lambdaQueryWrapper.eq(CourseTeacher::getCourseId, id);
        List<CourseTeacher> courseTeachers = courseTeacherMapper.selectList(lambdaQueryWrapper);
        return courseTeachers;
    }

    @Override
    public void deleteTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<CourseTeacher>();
        teacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId, courseId)
                .eq(CourseTeacher::getId, teacherId);

        CourseTeacher courseTeacher = courseTeacherMapper.selectOne(teacherLambdaQueryWrapper);
        if(courseTeacher==null){
            XueChengPlusException.cast("",200);
        }
        courseTeacherMapper.deleteById(courseTeacher);
    }
}
