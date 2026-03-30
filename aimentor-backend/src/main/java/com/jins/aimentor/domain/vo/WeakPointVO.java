package com.jins.aimentor.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("薄弱知识点")
public class WeakPointVO {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "知识点名称", example = "函数")
    private String knowledge;

    @ApiModelProperty(value = "掌握度百分比（0-100）", example = "45")
    private Integer mastery;

    @ApiModelProperty(value = "所属科目", example = "数学")
    private String subject;
}