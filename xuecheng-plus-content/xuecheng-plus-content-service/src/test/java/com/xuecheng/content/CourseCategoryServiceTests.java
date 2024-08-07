package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
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
public class CourseCategoryServiceTests {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Test
    public void testCourseCategoryService() {

        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.queryTreeNode("1");
        System.out.println(courseCategoryTreeDtos);

    }
}
