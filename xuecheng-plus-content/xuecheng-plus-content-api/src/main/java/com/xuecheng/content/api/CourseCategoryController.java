package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * 
 * @date 2023/2/11 15:44
 */
@Api(tags = "课程分类管理接口")
@RestController
public class CourseCategoryController {


    @Autowired
    CourseCategoryService courseCategoryService;


    @GetMapping("/course-category/tree-nodes")
    public List<CourseCategoryTreeDto> queryTreeNodes() {
        return courseCategoryService.queryTreeNodes("1");
    }


}
