package com.jins.aimentor.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 教学资源视图对象
 *
 * <p>⚠️ 注意：id 字段使用 Long 类型，但通过 @JsonSerialize 注解在序列化时转为 String，
 * 避免前端 JavaScript Number 精度丢失问题。
 */
@Data
public class ResourceVO {

    /**
     * 资源ID（Long 类型，序列化为字符串）
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
     * 资源类型（视频/文档/练习/音频）
     */
    private String type;

    /**
     * 资源链接
     */
    private String url;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 所属科目
     */
    private String subject;

    /**
     * 推荐指数
     */
    private Integer recommendScore;
}
