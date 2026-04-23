package com.jins.aimentor.service;

import com.jins.aimentor.domain.entity.LoginLog;
import com.jins.aimentor.domain.entity.OperationLog;

/**
 * 日志服务接口
 */
public interface LogService {
    
    /**
     * 记录登录日志
     */
    void recordLoginLog(LoginLog loginLog);
    
    /**
     * 记录操作日志
     */
    void recordOperationLog(OperationLog operationLog);
}
