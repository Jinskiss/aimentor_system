package com.jins.aimentor.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GeneratePlanDTO {

    /**
     * 科目名称
     */
    @NotBlank(message = "科目不能为空")
    private String subject;

    /**
     * 计划天数（Integer 必须用 @NotNull，不能用 @NotBlank——@NotBlank 仅适用于 CharSequence）
     */
    @NotNull(message = "天数不能为空")
    @Min(value = 1, message = "天数至少为1天")
    @Max(value = 365, message = "天数不能超过365天")
    private Integer days;
}