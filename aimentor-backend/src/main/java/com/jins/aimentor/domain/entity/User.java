package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 用户实体类
 *
 * <p>对应数据库表：user（表名不带t_前缀）</p>
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
     * 性别
     */
    private String gender;

    /**
     * 年级
     */
    private String grade;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 用户头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 累计学习天数
     */
    private Integer studyDays;

    /**
     * 累计学习分钟数
     */
    private Integer totalMinutes;

    /**
     * 用户等级（1-100）
     */
    private Integer level;

    /**
     * 经验值（达到一定值可升级）
     */
    private Integer experience;

    /**
     * 创建时间
     * <p>插入时自动填充</p>
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     * <p>插入和更新时自动填充</p>
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 逻辑删除标志
     * <p>0-未删除，1-已删除</p>
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}