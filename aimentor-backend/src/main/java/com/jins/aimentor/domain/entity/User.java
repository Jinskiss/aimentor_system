package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * <p>对应数据库表：t_user</p>
 * <p>使用MyBatis-Plus注解进行表映射和字段填充配置</p>
 *
 */
@TableName("user")
@Data
public class User {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 角色 student, teacher, admin
     */
    private String role;

    /**
     * 姓名
     */
    private String name;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 创建时间
     * <p>插入时自动填充</p>
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>插入和更新时自动填充</p>
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志
     * <p>0-未删除，1-已删除</p>
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}