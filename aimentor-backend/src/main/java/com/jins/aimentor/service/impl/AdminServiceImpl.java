package com.jins.aimentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jins.aimentor.domain.entity.LoginLog;
import com.jins.aimentor.domain.entity.OperationLog;
import com.jins.aimentor.domain.entity.Resource;
import com.jins.aimentor.domain.entity.User;
import com.jins.aimentor.mapper.LoginLogMapper;
import com.jins.aimentor.mapper.OperationLogMapper;
import com.jins.aimentor.mapper.ResourceMapper;
import com.jins.aimentor.mapper.UserMapper;
import com.jins.aimentor.service.AdminService;
import com.jins.aimentor.service.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<UserMapper, User> implements AdminService {

    private final UserMapper userMapper;
    private final ResourceMapper resourceMapper;
    private final OperationLogMapper operationLogMapper;
    private final LoginLogMapper loginLogMapper;
    private final ResourceService resourceService;

    // 用户状态：正常
    private static final String STATUS_ACTIVE = "active";
    // 资源状态：已发布
    private static final String RESOURCE_STATUS_PUBLISHED = "published";

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 统计总用户数
        Long totalUsers = userMapper.selectCount(null);
        stats.put("totalUsers", totalUsers);

        // 统计教师数量
        Long totalTeachers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "teacher")
        );
        stats.put("totalTeachers", totalTeachers);

        // 统计学生数量
        Long totalStudents = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "student")
        );
        stats.put("totalStudents", totalStudents);

        // 统计资源总数
        Long totalResources = resourceMapper.selectCount(null);
        stats.put("totalResources", totalResources);

        // 统计视频资源数量
        Long totalVideos = resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>()
                        .eq(Resource::getType, "video")
        );
        stats.put("totalVideos", totalVideos);

        // 统计文档资源数量
        Long totalDocs = resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>()
                        .eq(Resource::getType, "document")
        );
        stats.put("totalDocs", totalDocs);

        // 统计练习资源数量
        Long totalExercises = resourceMapper.selectCount(
                new LambdaQueryWrapper<Resource>()
                        .eq(Resource::getType, "exercise")
        );
        stats.put("totalExercises", totalExercises);

        // 计算增长率（模拟数据，实际应该对比历史数据）
        stats.put("userGrowth", 12);
        stats.put("teacherGrowth", 5);
        stats.put("resourceGrowth", 8);

        return stats;
    }

    @Override
    public List<Map<String, Object>> getUserGrowth(Integer days) {
        List<Map<String, Object>> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");

        for (int i = days - 1; i >= 0; i--) {
            Map<String, Object> dayData = new HashMap<>();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -i);

            dayData.put("date", sdf.format(cal.getTime()));

            // 模拟数据，实际应该从数据库查询
            dayData.put("users", (int) (Math.random() * 20) + 5);
            dayData.put("resources", (int) (Math.random() * 15) + 3);

            result.add(dayData);
        }

        return result;
    }

    @Override
    public Map<String, Object> getResourceStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计各类型资源数量
        List<Resource> resources = resourceMapper.selectList(null);

        Map<String, Long> typeCount = resources.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getType() != null ? r.getType() : "other",
                        Collectors.counting()
                ));

        stats.put("typeDistribution", typeCount);

        // 统计各科目资源数量
        Map<String, Long> subjectCount = resources.stream()
                .filter(r -> r.getSubject() != null && !r.getSubject().isEmpty())
                .collect(Collectors.groupingBy(
                        Resource::getSubject,
                        Collectors.counting()
                ));

        stats.put("subjectDistribution", subjectCount);

        return stats;
    }

    // ========== 用户管理 ==========

    @Override
    public Page<User> getUsers(Integer page, Integer size, String role, String status, String keyword) {
        Page<User> pageInfo = new Page<>(page, size);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 角色筛选
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }

        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getName, keyword)
                    .or()
                    .like(User::getPhone, keyword)
            );
        }

        wrapper.orderByDesc(User::getCreateTime);

        return userMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("[AdminService] 删除用户，id={}, 类型={}", id, id.getClass().getName());
        // 先查询确认记录存在
        User user = userMapper.selectById(id);
        log.info("[AdminService] 查询到的用户: {}", user);
        // 使用 Mapper 直接删除
        int rows = userMapper.deleteById(id);
        log.info("[AdminService] 删除影响行数: {}", rows);
    }

    @Override
    @Transactional
    public String resetPassword(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            String newPassword = "123456"; // 默认密码
            user.setPassword(newPassword);
            userMapper.updateById(user);
            return newPassword;
        }
        return null;
    }

    // ========== 资源管理 ==========

    @Override
    public Page<Resource> getResources(Integer page, Integer size, String type, String status, String subject, String keyword) {
        Page<Resource> pageInfo = new Page<>(page, size);

        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();

        // 类型筛选
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Resource::getType, type);
        }

        // 科目筛选
        if (subject != null && !subject.isEmpty()) {
            wrapper.eq(Resource::getSubject, subject);
        }

        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Resource::getTitle, keyword)
                    .or()
                    .like(Resource::getDescription, keyword)
            );
        }

        wrapper.orderByDesc(Resource::getCreateTime);

        return resourceMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Resource getResourceById(Long id) {
        return resourceMapper.selectById(id);
    }

    @Override
    @Transactional
    public void updateResource(Resource resource) {
        resourceMapper.updateById(resource);
    }

    @Override
    @Transactional
    public void deleteResource(Long id) {
        log.info("[AdminService] 删除资源，id={}, 类型={}", id, id.getClass().getName());
        // 先查询确认记录存在
        Resource resource = resourceMapper.selectById(id);
        log.info("[AdminService] 查询到的资源: {}", resource);
        // 使用 ResourceService 的 removeById，它会正确处理 @TableLogic
        boolean result = resourceService.deleteResource(id);
        log.info("[AdminService] 删除结果: {}", result);
    }

    @Override
    @Transactional
    public void auditResource(Long id, String status) {
        Resource resource = resourceMapper.selectById(id);
        if (resource != null) {
            resource.setRecommendScore(status.equals(RESOURCE_STATUS_PUBLISHED) ? 80 : 50);
            resourceMapper.updateById(resource);
        }
    }

    @Override
    @Transactional
    public void batchAuditResource(List<Long> ids, String status) {
        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                auditResource(id, status);
            }
        }
    }

    @Override
    @Transactional
    public void batchDeleteResource(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                resourceService.deleteResource(id);
            }
        }
    }

    // ========== 日志管理 ==========

    @Override
    public Page<OperationLog> getOperationLogs(Integer page, Integer size, String username, String operation, String status, String startDate, String endDate) {
        Page<OperationLog> pageInfo = new Page<>(page, size);

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        // 用户名搜索
        if (username != null && !username.isEmpty()) {
            wrapper.like(OperationLog::getUsername, username);
        }

        // 操作类型筛选
        if (operation != null && !operation.isEmpty()) {
            wrapper.eq(OperationLog::getOperation, operation);
        }

        // 状态筛选
        if (status != null && !status.isEmpty()) {
            wrapper.eq(OperationLog::getStatus, "1".equals(status) ? 1 : 0);
        }

        // 日期范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(OperationLog::getCreateTime, startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(OperationLog::getCreateTime, endDate + " 23:59:59");
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);

        return operationLogMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    public Page<LoginLog> getLoginLogs(Integer page, Integer size, String username, String status, String startDate, String endDate) {
        Page<LoginLog> pageInfo = new Page<>(page, size);

        LambdaQueryWrapper<LoginLog> wrapper = new LambdaQueryWrapper<>();

        // 用户名搜索
        if (username != null && !username.isEmpty()) {
            wrapper.like(LoginLog::getUsername, username);
        }

        // 状态筛选
        if (status != null && !status.isEmpty()) {
            wrapper.eq(LoginLog::getStatus, "1".equals(status) ? 1 : 0);
        }

        // 日期范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(LoginLog::getCreateTime, startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(LoginLog::getCreateTime, endDate + " 23:59:59");
        }

        wrapper.orderByDesc(LoginLog::getCreateTime);

        return loginLogMapper.selectPage(pageInfo, wrapper);
    }

    @Override
    @Transactional
    public void deleteOperationLog(Long id) {
        operationLogMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteLoginLog(Long id) {
        loginLogMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void clearOperationLogs() {
        operationLogMapper.delete(null);
    }

    @Override
    @Transactional
    public void clearLoginLogs() {
        loginLogMapper.delete(null);
    }
}
