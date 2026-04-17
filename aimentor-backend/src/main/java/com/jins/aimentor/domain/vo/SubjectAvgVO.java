package com.jins.aimentor.domain.vo;

import lombok.Data;

/**
 * 科目平均分 VO
 *
 * <p>用于教师 Dashboard 雷达图</p>
 */
@Data
public class SubjectAvgVO {

    /**
     * 科目名称
     */
    private String subject;

    /**
     * 该科目年级平均分
     */
    private Double avgScore;
}
