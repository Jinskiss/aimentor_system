package com.jins.aimentor.domain.vo;

import lombok.Data;

/**
 * 教师 Dashboard 统计数据 VO
 *
 * <p>返回给前端教师首页的汇总数据</p>
 */
@Data
public class TeacherDashboardVO {

    /**
     * 教师所在年级的学生总数
     */
    private Long studentCount;

    /**
     * 年级平均成绩
     */
    private Double avgScore;

    /**
     * 本月参考考试人次
     */
    private Long examCount;

    /**
     * 薄弱知识点数量（年级维度，统计所有学生的薄弱点总数）
     */
    private Long weakPointCount;

    /**
     * 本月成绩趋势数据（按月聚合）
     */
    private java.util.List<GradeScoreTrendVO> trendData;

    /**
     * 各科目平均分（雷达图用）
     */
    private java.util.List<SubjectAvgVO> subjectAvgs;

    /**
     * 待关注学生列表（平均分低于年级平均 20 分的学生）
     */
    private java.util.List<AttentionStudentVO> attentionStudents;
}
