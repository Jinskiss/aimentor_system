package com.jins.aimentor.domain.vo;

import lombok.Data;

/**
 * 年级成绩趋势 VO
 *
 * <p>用于教师 Dashboard 折线图，按月聚合所有学生的平均分</p>
 */
@Data
public class GradeScoreTrendVO {

    /**
     * 日期月份，如 "2026-01"
     */
    private String dateMonth;

    /**
     * 该月年级平均分
     */
    private Double avgScore;

    /**
     * 该月考试人次
     */
    private Long examCount;
}
