package com.xuecheng.content.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(value = "TeacherDto", description = "新增教室基本信息")
public class TeacherDto {
    private Long courseId;
    private String teacherName;
    private String position;
    private String introduction;


}
