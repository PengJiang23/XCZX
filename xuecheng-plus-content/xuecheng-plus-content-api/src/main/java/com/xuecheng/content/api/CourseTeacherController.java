package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.TeacherDto;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "师资管理接口")
@RestController
public class CourseTeacherController {

    @Autowired
    CourseTeacherService courseTeacherService;


    @ApiOperation("师资信息查询")
    @GetMapping("/courseTeacher/list/{id}")
    public List<CourseTeacher> queryTeacher(@PathVariable Long id){
        return courseTeacherService.getList(id);

    }



    @ApiOperation("添加师资信息")
    @PostMapping("/courseTeacher")
    public CourseTeacher queryTeacher(@RequestBody CourseTeacher courseTeacher){
        return courseTeacherService.addTeacher(courseTeacher);
    }


    @ApiOperation("修改师资信息")
    @PutMapping("/courseTeacher")
    public CourseTeacher updateTeacher(@RequestBody CourseTeacher courseTeacher){
        return courseTeacherService.addTeacher(courseTeacher);
    }

    @ApiOperation("删除师资信息")
    @DeleteMapping("/courseTeacher/course/{courseId}/{teacherId}")
    public void deleteTeacher(@PathVariable Long courseId, @PathVariable Long teacherId){
        courseTeacherService.deleteTeacher(courseId,teacherId);
    }


}
