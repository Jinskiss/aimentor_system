package com.jins.aimentor.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 学习资源实体类
 *
 * <p>对应数据库表：t_resource</p>
 * <p>存储推荐的学习资源（视频、文档等）</p>
 *
 */
@Data
@TableName("resource")
public class Resource {

    /**
     * 资源ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资源标题
     */
    private String title;

    /**
     * 资源描述
     */
    private String description;

    /**
     * 资源类型
     * <p>视频、文档、练习、音频等</p>
     */
    private String type;

    /**
     * 资源链接
     */
    private String url;

    /**
     * 所属科目
     */
    private String subject;

    /**
     * 推荐分数
     * <p>用于排序，分数越高越优先推荐</p>
     */
    private Integer recommendScore;

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