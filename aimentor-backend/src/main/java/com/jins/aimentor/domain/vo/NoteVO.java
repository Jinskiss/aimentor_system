package com.jins.aimentor.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 笔记视图对象
 *
 * <p>返回给前端的数据结构，不暴露数据库内部字段
 *
 * <p>⚠️ 注意：id 字段使用 Long 类型，但通过 @JsonSerialize 注解在序列化时转为 String，
 * 避免前端 JavaScript Number 精度丢失问题。
 */
@Data
public class NoteVO {

    /**
     * 笔记ID（Long 类型，序列化为字符串）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 所属科目
     */
    private String subject;

    /**
     * 标签列表
     */
    private String tags;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}
