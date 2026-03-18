package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("score_record")
public class ScoreRecord {
    
    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 学生ID */
    private Long studentId;
    
    /** 考试日期 */
    private LocalDate examDate;
    
    /** 科目 */
    private String subject;
    
    /** 分数 */
    private Integer score;
    
    /** 满分 */
    private Integer fullScore;

    /**
     * 日期月份（格式：yyyy-MM）
     * 用于成绩趋势统计查询，对应 SQL: DATE_FORMAT(exam_date, '%Y-%m') as date_month
     */
    @TableField(exist = false)  // 不是数据库字段
    private String dateMonth;

    /**
     * 平均分数
     * 用于成绩趋势统计查询，对应 SQL: ROUND(AVG(score), 0) as avg_score
     */
    @TableField(exist = false)  // 不是数据库字段
    private Integer avgScore;
}