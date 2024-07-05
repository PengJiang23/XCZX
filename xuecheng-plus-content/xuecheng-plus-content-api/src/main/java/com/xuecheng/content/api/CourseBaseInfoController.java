package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroup;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * 
 * @date 2023/2/11 15:44
 */
@Api(value = "课程信息管理接口", tags = "课程信息管理接口")
@RestController
public class CourseBaseInfoController {

    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程分页查询接口")
    @PostMapping("/course/list")
    @PreAuthorize("hasAuthority('xc_teachmanager_course_list')")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto) {
        return courseBaseService.queryCourseBaseList(pageParams, queryCourseParamsDto);

    }


    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated(value = ValidationGroup.Insert.class) AddCourseDto addCourseDto) {
        // 获取到用户所属机构的id
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(!StringUtils.isEmpty(user.getCompanyId())){
           companyId = Long.parseLong(user.getCompanyId());
        }
        // int i = 1/0;
        CourseBaseInfoDto courseBase = courseBaseInfoService.createCourseBase(companyId, addCourseDto);
        return courseBase;
    }


    @ApiOperation("根据id查询课程")
    @GetMapping("/course/{id}")
    public CourseBaseInfoDto queryById(@PathVariable Long id){
        // 获取当前用户身份
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        System.err.println(user);
        return courseBaseInfoService.getCourseBaseInfo(id);
    }


    @ApiOperation("修改课程")
    @PutMapping("/course")
    public CourseBaseInfoDto editCourse(@RequestBody EditCourseDto editCourseDto){

        // 获取到用户所属机构的id
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        Long companyId = null;
        if(!StringUtils.isEmpty(user.getCompanyId())){
            companyId = Long.parseLong(user.getCompanyId());
        }

        CourseBaseInfoDto courseBaseInfoDto = courseBaseInfoService.updateCourseBase(companyId, editCourseDto);
        return courseBaseInfoDto;
    }


    @ApiOperation("删除课程")
    @DeleteMapping("/course/{id}")
    public void deleteCourse(@PathVariable Long id){
        courseBaseInfoService.deleteInfo(id);
    }


}
