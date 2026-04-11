package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

/**
 * 学习计划实体类
 *
 * <p>对应数据库表：plan（表名不带t_前缀）</p>
 * <p>存储学生的学习计划安排</p>
 *
 */
@Data
@TableName("plan")
public class Plan {

    /**
     * 计划ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     * <p>关联user表，表示计划所有者</p>
     */
    private Long studentId;

    /**
     * 计划内容
     * <p>具体的学习任务描述</p>
     */
    private String content;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 计划状态
     * <p>未完成 / 已完成</p>
     */
    private String status;

    /**
     * 所属科目
     */
    private String subject;

    /**
     * 完成进度百分比(0-100)
     */
    private Integer progress;

    /**
     * 计划描述
     */
    private String description;

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
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
