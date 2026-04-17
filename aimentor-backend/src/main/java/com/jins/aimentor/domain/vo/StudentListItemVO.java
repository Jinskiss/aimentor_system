package com.jins.aimentor.domain.vo;

import lombok.Data;

/**
 * 学生列表项 VO
 *
 * <p>用于教师端左侧学生列表展示</p>
 */
@Data
public class StudentListItemVO {

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
     * 邮箱
     */
    private String email;

    /**
     * 该生平均成绩
     */
    private Double avgScore;

    /**
     * 薄弱知识点数量
     */
    private Integer weakPointCount;
}
