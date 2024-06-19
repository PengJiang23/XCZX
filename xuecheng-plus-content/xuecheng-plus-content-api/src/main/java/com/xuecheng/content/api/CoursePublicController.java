package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.CoursePreviewDTO;
import com.xuecheng.content.service.CoursePublishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CoursePublicController {
    @Autowired
    private CoursePublishService coursePublishService;



    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preivew(@PathVariable("courseId") Long courseId){
        ModelAndView modelAndView = new ModelAndView();
        CoursePreviewDTO coursePreviewDTO = coursePublishService.GetCourseInfo(courseId);
        modelAndView.setViewName("course_template");
        modelAndView.addObject("model", coursePreviewDTO);
        return modelAndView;
    }


    @ResponseBody
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        coursePublishService.commitAudit(1232141425L,courseId);
    }



}
