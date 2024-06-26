package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.model.vo.TeachPlanMediaVO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-05-05
 */
public interface TeachplanService extends IService<Teachplan> {


    public List<TeachplanDto> findTeachplanTree(Long courseId);

    void saveTeachplan(SaveTeachplanDto teachplan);

    void removePlanById(Long id);

    void planMoveDown(Long teachplanId);

    void planMoveUp(Long teachplanId);

    void associationMedia(TeachPlanMediaVO teachPlanMediaVO);

    void associationMediaCancel(String teachplanId, String mediaId);
}
