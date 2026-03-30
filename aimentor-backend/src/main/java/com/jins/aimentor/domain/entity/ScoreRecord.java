package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

/**
 * 成绩记录实体类
 *
 * <p>对应数据库表：score_record</p>
 * <p>存储学生的考试成绩信息</p>
 *
 */
@Data
@TableName("score_record")
public class ScoreRecord {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学生ID
     * <p>关联user表</p>
     */
    private Long studentId;

    /**
     * 考试日期
     */
    private LocalDate examDate;

    /**
     * 科目名称
     */
    private String subject;

    /**
     * 考试得分
     */
    private Integer score;

    /**
     * 满分分值
     * <p>默认100分</p>
     */
    private Integer fullScore;

    /**
     * 日期月份（格式：yyyy-MM）
     * <p>用于成绩趋势统计查询</p>
     */
    @TableField(exist = false)  // 不是数据库字段
    private String dateMonth;

    /**
     * 平均分数
     * <p>用于成绩趋势统计查询</p>
     */
    @TableField(exist = false)  // 不是数据库字段
    private Integer avgScore;

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