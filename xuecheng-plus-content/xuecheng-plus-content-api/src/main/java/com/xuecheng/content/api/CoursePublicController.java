package com.xuecheng.content.api;


import com.xuecheng.content.model.dto.CoursePreviewDTO;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.impl.CoursePublishServiceImpl;
import com.xuecheng.content.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
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
        // 获取到用户所属机构的id
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(!StringUtils.isEmpty(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }
        coursePublishService.commitAudit(companyId,courseId);
    }

    @ResponseBody
    @PostMapping("/coursepublish/{courseId}")
    public void commitpublish(@PathVariable("courseId") Long courseId){
        // 获取到用户所属机构的id
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(!StringUtils.isEmpty(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }
        coursePublishService.commitPublish(companyId,courseId);
    }

}
