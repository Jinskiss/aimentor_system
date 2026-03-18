package com.jins.aimentor.domain.vo;

import lombok.Data;

@Data
public class ScoreTrendVO {

    /**
     * 日期
     * <p>格式：yyyy-MM</p>
     */
    private String date;

    /**
     * 分数
     */
    private Integer score;
}