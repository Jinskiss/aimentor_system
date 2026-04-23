package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.constants.Status;
import com.jins.aimentor.domain.entity.LoginLog;
import com.jins.aimentor.domain.entity.OperationLog;
import com.jins.aimentor.domain.entity.Resource;
import com.jins.aimentor.domain.entity.User;
import com.jins.aimentor.exception.BizException;
import com.jins.aimentor.service.AdminService;
import com.jins.aimentor.service.ResourceService;
import com.jins.aimentor.service.UserService;
import com.jins.aimentor.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端控制器
 * 提供管理员专属的系统管理功能
 */
@SuppressWarnings("unchecked")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "管理端接口", description = "管理员使用的系统管理接口")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final ResourceService resourceService;

    /**
     * 检查是否为管理员（开发环境临时禁用）
     */
    private void checkAdmin() {
        // ===== 开发调试：临时禁用权限验证 =====
        // User currentUser = UserHolder.getUser();
        // if (currentUser == null || !"admin".equals(currentUser.getRole())) {
        //     throw new BizException(Status.CODE_403, "无权限访问，请使用管理员账号登录");
        // }
    }

    /**
     * 获取系统统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取统计数据", description = "获取系统各模块的统计数据")
    public Result<Map<String, Object>> getStatistics() {
        checkAdmin();
        return Result.success(adminService.getStatistics());
    }

    /**
     * 获取用户增长数据
     */
    @GetMapping("/analytics/user-growth")
    @Operation(summary = "获取用户增长趋势")
    public Result<List<Map<String, Object>>> getUserGrowth(@RequestParam(defaultValue = "7") Integer days) {
        checkAdmin();
        return Result.success(adminService.getUserGrowth(days));
    }

    /**
     * 获取资源统计数据
     */
    @GetMapping("/analytics/resource-stats")
    @Operation(summary = "获取资源统计")
    public Result<Map<String, Object>> getResourceStats() {
        checkAdmin();
        return Result.success(adminService.getResourceStats());
    }

    /**
     * 获取最新动态
     */
    @GetMapping("/analytics/recent-activities")
    @Operation(summary = "获取最新动态")
    public Result<List<Map<String, Object>>> getRecentActivities() {
        checkAdmin();
        return Result.success(adminService.getRecentActivities());
    }

    // ========== 用户管理 ==========

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    @Operation(summary = "获取用户列表")
    public Result<Page<User>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        checkAdmin();
        Page<User> result = adminService.getUsers(page, size, role, status, keyword);
        return Result.success(result);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    @Operation(summary = "获取用户详情")
    public Result<User> getUserById(@PathVariable String id) {
        checkAdmin();
        User user = adminService.getUserById(parseId(id));
        return user != null ? Result.success(user) : Result.error("用户不存在");
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/users/{id}")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateUser(@PathVariable String id, @RequestBody User user) {
        checkAdmin();
        user.setId(parseId(id));
        adminService.updateUser(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> deleteUser(@PathVariable String id) {
        checkAdmin();
        adminService.deleteUser(parseId(id));
        return Result.success();
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/users/{id}/reset-password")
    @Operation(summary = "重置用户密码")
    public Result<String> resetPassword(@PathVariable String id) {
        checkAdmin();
        String newPassword = adminService.resetPassword(parseId(id));
        return Result.success(newPassword);
    }

    /**
     * 批量修复用户数据（姓名、性别、头像）
     */
    @PostMapping("/users/fix-data")
    @Operation(summary = "批量修复用户数据")
    public Result<Map<String, Object>> fixUserData() {
        checkAdmin();
        List<User> users = userService.list();

        int fixed = 0;
        for (User user : users) {
            boolean needUpdate = false;

            // 修复姓名为空或乱码
            if (user.getName() == null || user.getName().isEmpty() ||
                user.getName().length() != user.getName().getBytes().length / 3) {
                user.setName(user.getUsername());
                needUpdate = true;
            }

            // 修复性别
            if (user.getGender() == null || user.getGender().isEmpty() ||
                (!"男".equals(user.getGender()) && !"女".equals(user.getGender()) && !"保密".equals(user.getGender()))) {
                user.setGender("保密");
                needUpdate = true;
            }

            // 修复头像
            if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                user.setAvatar("https://api.dicebear.com/7.x/initials/svg?seed=" + user.getUsername() +
                              "&backgroundColor=409eff,67c23a,e6a23c,f56c6c,909399&textColor=ffffff");
                needUpdate = true;
            }

            if (needUpdate) {
                userService.updateById(user);
                fixed++;
            }
        }

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("total", users.size());
        result.put("fixed", fixed);
        return Result.success(result);
    }

    // ========== 资源管理 ==========

    /**
     * 获取资源列表
     */
    @GetMapping("/resources")
    @Operation(summary = "获取资源列表")
    public Result<Page<Resource>> getResources(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String keyword) {
        checkAdmin();
        Page<Resource> result = adminService.getResources(page, size, type, status, subject, keyword);
        return Result.success(result);
    }

    /**
     * 获取资源详情
     */
    @GetMapping("/resources/{id}")
    @Operation(summary = "获取资源详情")
    public Result<Resource> getResourceById(@PathVariable String id) {
        checkAdmin();
        Resource resource = adminService.getResourceById(parseId(id));
        return resource != null ? Result.success(resource) : Result.error("资源不存在");
    }

    /**
     * 更新资源信息
     */
    @PutMapping("/resources/{id}")
    @Operation(summary = "更新资源信息")
    public Result<Void> updateResource(@PathVariable String id, @RequestBody Resource resource) {
        checkAdmin();
        resource.setId(parseId(id));
        adminService.updateResource(resource);
        return Result.success();
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/resources/{id}")
    @Operation(summary = "删除资源")
    public Result<Void> deleteResource(@PathVariable String id) {
        checkAdmin();
        adminService.deleteResource(parseId(id));
        return Result.success();
    }

    /**
     * 审核资源
     */
    @PostMapping("/resources/{id}/audit")
    @Operation(summary = "审核资源")
    public Result<Void> auditResource(@PathVariable String id, @RequestParam String status) {
        checkAdmin();
        adminService.auditResource(parseId(id), status);
        return Result.success();
    }

    /**
     * 批量审核资源
     */
    @PostMapping("/resources/batch-audit")
    @Operation(summary = "批量审核资源")
    public Result<Void> batchAudit(@RequestBody Map<String, Object> params) {
        checkAdmin();
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) params.get("ids");
        String status = (String) params.get("status");
        adminService.batchAuditResource(ids.stream().map(this::parseId).toList(), status);
        return Result.success();
    }

    /**
     * 批量删除资源
     */
    @PostMapping("/resources/batch-delete")
    @Operation(summary = "批量删除资源")
    public Result<Void> batchDelete(@RequestBody Map<String, Object> params) {
        checkAdmin();
        @SuppressWarnings("unchecked")
        List<String> ids = (List<String>) params.get("ids");
        adminService.batchDeleteResource(ids.stream().map(this::parseId).toList());
        return Result.success();
    }

    // ========== 日志管理 ==========

    /**
     * 获取操作日志列表
     */
    @GetMapping("/logs/operation")
    @Operation(summary = "获取操作日志列表")
    public Result<Page<OperationLog>> getOperationLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        checkAdmin();
        Page<OperationLog> result = adminService.getOperationLogs(page, size, username, operation, status, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 获取登录日志列表
     */
    @GetMapping("/logs/login")
    @Operation(summary = "获取登录日志列表")
    public Result<Page<LoginLog>> getLoginLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        checkAdmin();
        Page<LoginLog> result = adminService.getLoginLogs(page, size, username, status, startDate, endDate);
        return Result.success(result);
    }

    /**
     * 删除操作日志
     */
    @DeleteMapping("/logs/operation/{id}")
    @Operation(summary = "删除操作日志")
    public Result<Void> deleteOperationLog(@PathVariable String id) {
        checkAdmin();
        adminService.deleteOperationLog(parseId(id));
        return Result.success();
    }

    /**
     * 删除登录日志
     */
    @DeleteMapping("/logs/login/{id}")
    @Operation(summary = "删除登录日志")
    public Result<Void> deleteLoginLog(@PathVariable String id) {
        checkAdmin();
        adminService.deleteLoginLog(parseId(id));
        return Result.success();
    }

    /**
     * 清空操作日志
     */
    @DeleteMapping("/logs/operation")
    @Operation(summary = "清空操作日志")
    public Result<Void> clearOperationLogs() {
        checkAdmin();
        adminService.clearOperationLogs();
        return Result.success();
    }

    /**
     * 清空登录日志
     */
    @DeleteMapping("/logs/login")
    @Operation(summary = "清空登录日志")
    public Result<Void> clearLoginLogs() {
        checkAdmin();
        adminService.clearLoginLogs();
        return Result.success();
    }

    /**
     * 获取最近的日志（合并操作日志和登录日志）
     */
    @GetMapping("/logs/recent")
    @Operation(summary = "获取最近的日志")
    public Result<List<Map<String, Object>>> getRecentLogs() {
        checkAdmin();
        return Result.success(adminService.getRecentLogs());
    }

    /**
     * 将字符串ID转换为Long
     */
    private Long parseId(String id) {
        return Long.parseLong(id);
    }
}
