package com.jins.aimentor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jins.aimentor.domain.entity.StudyPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 学习计划Mapper接口
 *
 * <p>继承BaseMapper获得基础CRUD能力</p>
 *
 * @author Education Team
 * @since 1.0.0
 */
@Mapper
public interface StudyPlanMapper extends BaseMapper<StudyPlan> {

    /**
     * 更新计划状态（带用户权限验证）
     *
     * <p>确保用户只能更新自己的计划</p>
     * <p>返回影响行数，0表示计划不存在或无权限</p>
     *
     * @param id 计划ID
     * @param studentId 学生ID（用于权限验证）
     * @param status 新状态
     * @return 影响行数
     */
    @Update("UPDATE study_plan " +
            "SET status = #{status} " +
            "WHERE id = #{id} AND student_id = #{studentId} AND deleted = 0")
    int updateStatus(@Param("id") Long id,
                     @Param("studentId") Long studentId,
                     @Param("status") String status);
}