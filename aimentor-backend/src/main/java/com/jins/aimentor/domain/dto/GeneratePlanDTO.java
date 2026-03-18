package com.jins.aimentor.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GeneratePlanDTO {

    /**
     * 科目名称
     */
    @NotNull(message = "科目不能为空")
    private String subject;

    /**
     * 计划天数
     */
    @NotNull(message = "天数不能为空")
    @Min(value = 1, message = "天数至少为1天")
    private Integer days;
}