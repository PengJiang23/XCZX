package com.xuecheng.content.api;


import com.google.j2objc.annotations.AutoreleasePool;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Api(tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @Value("${test.name}")
    String test;


    @ApiOperation("查询课程计划树型结构")
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId){

      return  teachplanService.findTeachplanTree(courseId);
    }


    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan( @RequestBody SaveTeachplanDto teachplan){
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("课程计划删除")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachplan(@PathVariable("id") Long teachplanId){
        teachplanService.removePlanById(teachplanId);
    }


    @ApiOperation("课程计划排序下移")
    @PostMapping("/teachplan/movedown/{id}")
    public void planMoveDown(@PathVariable("id") Long teachplanId){
        teachplanService.planMoveDown(teachplanId);
    }


    @ApiOperation("课程计划排序上移")
    @PostMapping("/teachplan/moveup/{id}")
    public void planMoveUp(@PathVariable("id") Long teachplanId){
        teachplanService.planMoveUp(teachplanId);
    }


    @GetMapping("/hello")
    public String plan(){
        return test;
    }


}
