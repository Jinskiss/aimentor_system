package com.jins.aimentor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jins.aimentor.domain.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
