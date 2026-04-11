package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 学习笔记实体类
 *
 * <p>对应数据库表：note</p>
 * <p>存储用户创建的学习笔记，支持按科目归档和标签分类</p>
 */
@Data
@TableName("note")
public class Note {

    /**
     * 笔记ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容（支持 Markdown 富文本）
     */
    private String content;

    /**
     * 所属科目
     */
    private String subject;

    /**
     * 标签列表（多个标签用逗号分隔）
     */
    private String tags;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
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
