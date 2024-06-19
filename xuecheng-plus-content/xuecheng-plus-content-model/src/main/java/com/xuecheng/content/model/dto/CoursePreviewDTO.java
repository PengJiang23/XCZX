package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Data
@Slf4j
public class CoursePreviewDTO {
    // 课程基本信息
    private CourseBaseInfoDto courseBase;

    // 课程计划
    private List<TeachplanDto> teachplans;

    // 师资信息 todo
}
