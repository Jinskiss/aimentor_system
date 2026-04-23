package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 登录日志实体类
 */
@TableName("login_log")
@Data
public class LoginLog {
    
    /**
     * 日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户角色
     */
    private String role;
    
    /**
     * 登录状态 0-失败 1-成功
     */
    private Integer status;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 登录地点
     */
    private String location;
    
    /**
     * 操作系统
     */
    private String os;
    
    /**
     * 浏览器
     */
    private String browser;
    
    /**
     * 错误消息
     */
    private String msg;
    
    /**
     * 登录时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
