package com.jins.aimentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jins.aimentor.domain.dto.GeneratePlanDTO;
import com.jins.aimentor.domain.entity.Plan;
import com.jins.aimentor.domain.entity.User;
import com.jins.aimentor.mapper.PlanMapper;
import com.jins.aimentor.service.PlanService;
import com.jins.aimentor.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanMapper planMapper;

    @Value("${ai.service.base-url:http://127.0.0.1:8000}")
    private String aiServiceBaseUrl;

    @Value("${ai.service.timeout-seconds:60}")
    private int timeoutSeconds;

    @Override
    public Plan generatePlan(GeneratePlanDTO dto) {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }

        log.info("[PlanService] 开始生成学习计划，subject={}, days={}, studentId={}",
                dto.getSubject(), dto.getDays(), user.getId());

        // 调用 AI 服务生成计划内容
        String planContent = callAiToGeneratePlan(dto.getSubject(), dto.getDays());

        // 构建学习计划实体
        Plan plan = new Plan();
        plan.setStudentId(user.getId());
        plan.setSubject(dto.getSubject());
        plan.setContent(planContent);
        plan.setStartDate(LocalDate.now());
        plan.setEndDate(LocalDate.now().plusDays(dto.getDays()));
        plan.setStatus("未完成");

        // 保存到数据库
        planMapper.insert(plan);
        log.info("[PlanService] 学习计划保存成功，planId={}", plan.getId());

        return plan;
    }

    @Override
    public List<Plan> getMyPlans() {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }

        log.debug("[PlanService] 获取学习计划列表，studentId={}", user.getId());
        return planMapper.selectByStudentId(user.getId());
    }

    @Override
    public Plan getPlanById(Long id) {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }

        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plan::getId, id)
               .eq(Plan::getStudentId, user.getId())
               .eq(Plan::getDeleted, 0);
        return planMapper.selectOne(wrapper);
    }

    @Override
    public void updateStatus(Long id, String status) {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }

        int rows = planMapper.updateStatus(id, user.getId(), status);
        if (rows == 0) {
            throw new RuntimeException("计划不存在或无权限修改");
        }
        log.info("[PlanService] 更新计划状态成功，id={}, status={}", id, status);
    }

    @Override
    public void markAsCompleted(Long id) {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }

        int rows = planMapper.markAsCompleted(id, user.getId());
        if (rows == 0) {
            throw new RuntimeException("计划不存在或无权限修改");
        }
        log.info("[PlanService] 标记计划为已完成，id={}", id);
    }

    @Override
    public void deletePlan(Long id) {
        User user = UserHolder.getUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }

        // 逻辑删除（MyBatis-Plus 的 @TableLogic 会自动处理）
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plan::getId, id)
               .eq(Plan::getStudentId, user.getId());
        int rows = planMapper.delete(wrapper);

        if (rows == 0) {
            throw new RuntimeException("计划不存在或无权限删除");
        }
        log.info("[PlanService] 删除学习计划成功，id={}", id);
    }

    /**
     * 调用 AI 服务生成学习计划内容
     *
     * @param subject 科目名称
     * @param days 计划天数
     * @return AI 生成的计划内容
     */
    private String callAiToGeneratePlan(String subject, Integer days) {
        String prompt = String.format(
            "你是一个高中学习规划专家。请为学生制定一个%d天的%s学习计划。" +
            "要求：\n" +
            "1. 每天有具体的学习目标和任务\n" +
            "2. 涵盖知识点梳理、练习、复习等环节\n" +
            "3. 合理安排时间，保持循序渐进\n" +
            "4. 输出格式清晰，使用列表形式\n\n" +
            "请按以下格式输出（每天一行）：\n" +
            "Day 1: [具体任务]\n" +
            "Day 2: [具体任务]\n" +
            "...\n",
            days, subject
        );

        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, String> body = new HashMap<>();
            body.put("message", prompt);
            body.put("session_id", "plan_" + System.currentTimeMillis());

            String url = aiServiceBaseUrl + "/api/chat";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity<Map<String, String>> entity =
                new org.springframework.http.HttpEntity<>(body, headers);

            var response = restTemplate.exchange(url, org.springframework.http.HttpMethod.POST,
                entity, String.class);

            if (response.getStatusCode().value() == 200 && response.getBody() != null) {
                com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                var node = objectMapper.readTree(response.getBody());
                String answer = node.get("answer").asText();
                log.info("[PlanService] AI 生成计划成功，内容长度={}", answer.length());
                return answer;
            } else {
                throw new RuntimeException("AI 服务响应异常");
            }
        } catch (Exception e) {
            log.error("[PlanService] 调用 AI 服务失败：{}", e.getMessage());
            // 如果 AI 服务不可用，返回默认计划模板
            return generateDefaultPlan(subject, days);
        }
    }

    /**
     * 生成默认学习计划（当 AI 服务不可用时）
     */
    private String generateDefaultPlan(String subject, Integer days) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(subject).append(" 学习计划（共").append(days).append("天）\n\n");

        for (int i = 1; i <= days; i++) {
            sb.append("**Day ").append(i).append("**\n");
            sb.append("- 复习").append(subject).append("基础知识\n");
            sb.append("- 完成课后练习题\n");
            if (i % 3 == 0) {
                sb.append("- 整理错题本\n");
            }
            if (i % 5 == 0) {
                sb.append("- 进行阶段性测试\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
