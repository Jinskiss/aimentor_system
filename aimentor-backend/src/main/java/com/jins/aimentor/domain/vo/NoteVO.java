package com.jins.aimentor.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 笔记视图对象
 *
 * <p>返回给前端的数据结构，不暴露数据库内部字段</p>
 */
@Data
public class NoteVO {

    /**
     * 笔记ID
     */
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
