package com.jins.aimentor.service.impl;

import com.jins.aimentor.domain.entity.LoginLog;
import com.jins.aimentor.domain.entity.OperationLog;
import com.jins.aimentor.mapper.LoginLogMapper;
import com.jins.aimentor.mapper.OperationLogMapper;
import com.jins.aimentor.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 日志服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LoginLogMapper loginLogMapper;
    private final OperationLogMapper operationLogMapper;

    @Override
    public void recordLoginLog(LoginLog loginLog) {
        try {
            loginLogMapper.insert(loginLog);
            log.info("记录登录日志成功: username={}, status={}", loginLog.getUsername(), loginLog.getStatus());
        } catch (Exception e) {
            log.error("记录登录日志失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void recordOperationLog(OperationLog operationLog) {
        try {
            operationLogMapper.insert(operationLog);
            log.info("记录操作日志成功: username={}, operation={}", operationLog.getUsername(), operationLog.getOperation());
        } catch (Exception e) {
            log.error("记录操作日志失败: {}", e.getMessage(), e);
        }
    }
}
