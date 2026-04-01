package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.domain.dto.GeneratePlanDTO;
import com.jins.aimentor.domain.entity.Plan;
import com.jins.aimentor.service.PlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/plan")
@RequiredArgsConstructor
@Api(tags = "学习计划", description = "个性化学习计划管理接口")
public class PlanController {

    private final PlanService planService;

    @ApiOperation("生成学习计划")
    @PostMapping("/generate")
    public Result<Plan> generate(@Valid @RequestBody GeneratePlanDTO dto) {
        log.info("[PlanController] 收到生成计划请求，subject={}, days={}", dto.getSubject(), dto.getDays());

        try {
            Plan plan = planService.generatePlan(dto);
            log.info("[PlanController] 学习计划生成成功，planId={}", plan.getId());
            return Result.success("计划生成成功", plan);
        } catch (RuntimeException e) {
            log.error("[PlanController] 生成学习计划失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("获取我的学习计划")
    @GetMapping("/my")
    public Result<List<Plan>> getMyPlans() {
        log.debug("[PlanController] 获取当前用户的学习计划列表");

        try {
            List<Plan> plans = planService.getMyPlans();
            log.info("[PlanController] 获取到 {} 条学习计划", plans.size());
            return Result.success(plans);
        } catch (RuntimeException e) {
            log.error("[PlanController] 获取学习计划失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("获取学习计划详情")
    @GetMapping("/{id}")
    public Result<Plan> getPlanById(@PathVariable Long id) {
        log.debug("[PlanController] 获取计划详情，id={}", id);

        try {
            Plan plan = planService.getPlanById(id);
            if (plan == null) {
                return Result.error("计划不存在");
            }
            return Result.success(plan);
        } catch (RuntimeException e) {
            log.error("[PlanController] 获取计划详情失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("更新学习计划状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("[PlanController] 更新计划状态，id={}, status={}", id, status);

        try {
            planService.updateStatus(id, status);
            return Result.success("状态更新成功", null);
        } catch (RuntimeException e) {
            log.error("[PlanController] 更新计划状态失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("标记计划为已完成")
    @PutMapping("/{id}/complete")
    public Result<Void> completePlan(@PathVariable Long id) {
        log.info("[PlanController] 标记计划为已完成，id={}", id);

        try {
            planService.markAsCompleted(id);
            return Result.success("已标记完成", null);
        } catch (RuntimeException e) {
            log.error("[PlanController] 标记完成失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("删除学习计划")
    @DeleteMapping("/{id}")
    public Result<Void> deletePlan(@PathVariable Long id) {
        log.info("[PlanController] 删除学习计划，id={}", id);

        try {
            planService.deletePlan(id);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            log.error("[PlanController] 删除计划失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
