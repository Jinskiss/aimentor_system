package com.jins.aimentor.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("成绩趋势")
public class ScoreTrendVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "日期月份", example = "2024-03")
    private String dateMonth;

    @ApiModelProperty(value = "该月平均分数", example = "85")
    private Integer avgScore;
}