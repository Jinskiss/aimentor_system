package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 知识点掌握度实体类
 *
 * <p>对应数据库表：knowledge_mastery
 * <p>记录学生对各知识点的掌握程度（0-100）
 *
 * <p>⚠️ 注意：id 和 studentId 字段添加了 @JsonSerialize 注解，在序列化时转为 String，
 * 避免前端 JavaScript Number 精度丢失问题。
 */
@Data
@TableName("knowledge_mastery")
public class KnowledgeMastery {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 学生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;

    /**
     * 知识点名称
     * <p>如：函数、概率、几何等</p>
     */
    private String knowledge;

    /**
     * 掌握度百分比
     * <p>范围：0-100，数值越高掌握越好</p>
     */
    private Integer mastery;

    /**
     * 所属科目
     */
    private String subject;

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
