package com.jins.aimentor.domain.entity;

/**
 * 学习计划实体
 */
public class Plan {

    private Integer id;

    /**
     * 计划内容
     */
    private String content;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 状态（未完成 / 已完成）
     */
    private String status;

    /**
     * 用户ID
     */
    private Integer userId;

}