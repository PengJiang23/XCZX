package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.CoursePreviewDTO;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/open")
public class CourseOpenController {

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private CoursePublishService coursePublishService;


    @GetMapping("/course/whole/{courseId}")
    public CoursePreviewDTO getPreviewInfo(@PathVariable("courseId") Long courseId){
        CoursePreviewDTO coursePreviewDTO = coursePublishService.GetCourseInfo(courseId);
        return coursePreviewDTO;
    }

}
