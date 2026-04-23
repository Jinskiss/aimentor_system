package com.jins.aimentor.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jins.aimentor.domain.entity.LoginLog;
import com.jins.aimentor.domain.entity.OperationLog;
import com.jins.aimentor.domain.entity.Resource;
import com.jins.aimentor.domain.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 管理端服务接口
 */
public interface AdminService extends IService<User> {

    /**
     * 获取系统统计数据
     */
    Map<String, Object> getStatistics();

    /**
     * 获取用户增长数据
     */
    List<Map<String, Object>> getUserGrowth(Integer days);

    /**
     * 获取资源统计数据
     */
    Map<String, Object> getResourceStats();

    /**
     * 获取最新动态
     */
    List<Map<String, Object>> getRecentActivities();

    // ========== 用户管理 ==========

    /**
     * 获取用户列表（分页）
     */
    Page<User> getUsers(Integer page, Integer size, String role, String status, String keyword);

    /**
     * 获取用户详情
     */
    User getUserById(Long id);

    /**
     * 更新用户信息
     */
    void updateUser(User user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 重置用户密码
     */
    String resetPassword(Long id);

    // ========== 资源管理 ==========

    /**
     * 获取资源列表（分页）
     */
    Page<Resource> getResources(Integer page, Integer size, String type, String status, String subject, String keyword);

    /**
     * 获取资源详情
     */
    Resource getResourceById(Long id);

    /**
     * 更新资源信息
     */
    void updateResource(Resource resource);

    /**
     * 删除资源
     */
    void deleteResource(Long id);

    /**
     * 审核资源
     */
    void auditResource(Long id, String status);

    /**
     * 批量审核资源
     */
    void batchAuditResource(List<Long> ids, String status);

    /**
     * 批量删除资源
     */
    void batchDeleteResource(List<Long> ids);

    // ========== 日志管理 ==========

    /**
     * 获取操作日志列表（分页）
     */
    Page<OperationLog> getOperationLogs(Integer page, Integer size, String username, String operation, String status, String startDate, String endDate);

    /**
     * 获取登录日志列表（分页）
     */
    Page<LoginLog> getLoginLogs(Integer page, Integer size, String username, String status, String startDate, String endDate);

    /**
     * 删除操作日志
     */
    void deleteOperationLog(Long id);

    /**
     * 删除登录日志
     */
    void deleteLoginLog(Long id);

    /**
     * 清空操作日志
     */
    void clearOperationLogs();

    /**
     * 清空登录日志
     */
    void clearLoginLogs();

    /**
     * 获取最近的日志（合并操作日志和登录日志）
     */
    List<Map<String, Object>> getRecentLogs();
}
