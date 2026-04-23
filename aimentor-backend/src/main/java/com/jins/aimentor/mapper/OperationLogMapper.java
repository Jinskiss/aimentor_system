package com.jins.aimentor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jins.aimentor.domain.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
