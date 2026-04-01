package com.jins.aimentor.service;

import com.jins.aimentor.domain.dto.GeneratePlanDTO;
import com.jins.aimentor.domain.entity.Plan;

import java.util.List;

public interface PlanService {

    /**
     * 生成学习计划
     *
     * @param dto 生成参数（科目、天数等）
     * @return 生成的学习计划
     */
    Plan generatePlan(GeneratePlanDTO dto);

    /**
     * 获取当前用户的所有学习计划
     *
     * @return 学习计划列表
     */
    List<Plan> getMyPlans();

    /**
     * 根据ID获取学习计划
     *
     * @param id 计划ID
     * @return 学习计划
     */
    Plan getPlanById(Long id);

    /**
     * 更新学习计划状态
     *
     * @param id 计划ID
     * @param status 新状态
     */
    void updateStatus(Long id, String status);

    /**
     * 标记学习计划为已完成
     *
     * @param id 计划ID
     */
    void markAsCompleted(Long id);

    /**
     * 删除学习计划
     *
     * @param id 计划ID
     */
    void deletePlan(Long id);
}
