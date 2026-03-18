package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("knowledge_mastery")
public class KnowledgeMastery {
    
    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 学生ID */
    private Long studentId;
    
    /** 知识点名称 */
    private String knowledge;
    
    /** 掌握度百分比（0-100） */
    private Integer mastery;
    
    /** 所属科目 */
    private String subject;
}