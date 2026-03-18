package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("study_plan")
public class StudyPlan {
    
    /** 计划ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 学生ID */
    private Long studentId;
    
    /** 计划内容 */
    private String content;
    
    /** 开始日期 */
    private LocalDate startDate;
    
    /** 结束日期 */
    private LocalDate endDate;
    
    /** 状态：未完成/已完成 */
    private String status;
    
    /** 所属科目 */
    private String subject;
    
    /** 创建时间 */
    private LocalDateTime createTime;
}
