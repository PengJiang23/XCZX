package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * 
 * @date 2023/2/12 9:24
 */
@SpringBootTest
public class TeachplanMapperTests {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Test
    public void testCourseBaseMapper() {

        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(25L);
        for (TeachplanDto teachplanDto : teachplanDtos) {
            System.err.println(teachplanDto.getPname());
            for (TeachplanDto teachPlanTreeNode : teachplanDto.getTeachPlanTreeNodes()) {
                System.err.println(teachPlanTreeNode.getPname());
            }

        }
    }
}
