package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {


    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {

        return teachplanMapper.selectTreeNodes(courseId);
    }


    /**
     * 获取课程计划的order by
     *
     * @param courseId
     * @param parentId
     * @return
     */

    private int getTeachplanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper
                .eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getParentid, parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count + 1;
    }


    /**
     * 新增/修改课程计划
     *
     * @param saveTeachplanDto
     */
    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        //通过课程计划id判断是新增和修改
        Long teachplanId = saveTeachplanDto.getId();
        if (teachplanId == null) {
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            //确定排序字段，找到它的同级节点个数，排序字段就是个数加1  select count(1) from teachplan where course_id=117 and parentid=268
            Long parentid = saveTeachplanDto.getParentid();
            Long courseId = saveTeachplanDto.getCourseId();
            int teachplanCount = getTeachplanCount(courseId, parentid);
            teachplan.setOrderby(teachplanCount);
            teachplanMapper.insert(teachplan);

        } else {
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachplanId);
            //将参数复制到teachplan
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }


    @Override
    public void removePlanById(Long id) {
        // todo
        // 根据id判断是大章节还是小章节
        // 大章节， 如果下面没有小章节直接删除，否则报错返回异常信息
        // 小章节，删除，并将关联media表也相应删除

        Teachplan teachplan = teachplanMapper.selectById(id);
        Long courseId = teachplan.getCourseId();

        Long parentid = teachplan.getParentid();
        if (parentid != 0) {
            String mediaType = teachplan.getMediaType();
            if (mediaType == null) {
                teachplanMapper.deleteById(teachplan);
            } else {
                LambdaQueryWrapper<TeachplanMedia> teachplanMediaQueryWrapper = new LambdaQueryWrapper<TeachplanMedia>();
                teachplanMediaQueryWrapper.eq(TeachplanMedia::getTeachplanId, id)
                        .eq(TeachplanMedia::getCourseId, courseId);
                teachplanMediaMapper.delete(teachplanMediaQueryWrapper);
                teachplanMapper.deleteById(teachplan);
            }

        } else {
            Long count = teachplanMapper.selectTwoCount(id);
            if (count == 0) {
                teachplanMapper.deleteById(teachplan);
            } else {
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作", 120409);
            }

        }

    }


    /**
     * 课程计划排序
     *
     * @param teachplanId
     */
    @Override
    public void planMoveDown(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        // 如果是小章节，找到orderby+1的计划，如果存在则实现交换
        // 如果是大章节，找到orderby+1的计划，。。
        extractedDown(teachplan);


    }

    private void extractedDown(Teachplan teachplan) {
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getParentid, teachplan.getParentid())
                .eq(Teachplan::getGrade, teachplan.getGrade())
                .eq(Teachplan::getOrderby, teachplan.getOrderby() + 1)
                .eq(Teachplan::getCourseId, teachplan.getCourseId());

        Teachplan downTeachplan = teachplanMapper.selectOne(teachplanLambdaQueryWrapper);
        if (downTeachplan != null) {
            teachplan.setOrderby(teachplan.getOrderby() + 1);
            downTeachplan.setOrderby(downTeachplan.getOrderby() - 1);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(downTeachplan);
        }else{
            XueChengPlusException.cast("到底了，打住");
        }
    }


    public void planMoveUp(Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        // 根据parentid+grade相等，且order+1的课程；如果不存在则不变，否则交换两个值
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getParentid, teachplan.getParentid())
                .eq(Teachplan::getGrade, teachplan.getGrade())
                .eq(Teachplan::getOrderby, teachplan.getOrderby() - 1)
                .eq(Teachplan::getCourseId, teachplan.getCourseId());

        Teachplan downTeachplan = teachplanMapper.selectOne(teachplanLambdaQueryWrapper);

        if (downTeachplan != null) {
            teachplan.setOrderby(teachplan.getOrderby() - 1);
            downTeachplan.setOrderby(downTeachplan.getOrderby() + 1);
            teachplanMapper.updateById(teachplan);
            teachplanMapper.updateById(downTeachplan);
        }else {
            XueChengPlusException.cast("到头了，别点了");
        }

    }


}
