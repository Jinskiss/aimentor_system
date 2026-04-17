package com.jins.aimentor.domain.vo;

import lombok.Data;

/**
 * 待关注学生 VO
 *
 * <p>用于教师 Dashboard 待关注学生列表</p>
 */
@Data
public class AttentionStudentVO {

    /**
     * 学生 ID
     */
    private Long id;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 年级
     */
    private String grade;

    /**
     * 该生平均成绩
     */
    private Double avgScore;

    /**
     * 薄弱知识点数量
     */
    private Integer weakPointCount;

    /**
     * 主要薄弱科目
     */
    private String weakSubject;
}
