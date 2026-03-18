package com.jins.aimentor.domain.vo;

import lombok.Data;

@Data
public class StudyPlanVO {

    /**
     * 计划ID
     */
    private Long id;

    /**
     * 计划内容
     */
    private String content;

    /**
     * 开始日期
     * <p>格式：yyyy-MM-dd</p>
     */
    private String startDate;

    /**
     * 结束日期
     * <p>格式：yyyy-MM-dd</p>
     */
    private String endDate;

    /**
     * 计划状态
     * <p>未完成 / 已完成</p>
     */
    private String status;
}