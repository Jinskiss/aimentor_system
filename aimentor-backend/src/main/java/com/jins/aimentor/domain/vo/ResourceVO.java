package com.jins.aimentor.domain.vo;

import lombok.Data;

@Data
public class ResourceVO {

    /**
     * 资源ID
     */
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
     * 所属科目
     */
    private String subject;

    /**
     * 推荐指数
     */
    private Integer recommendScore;
}
