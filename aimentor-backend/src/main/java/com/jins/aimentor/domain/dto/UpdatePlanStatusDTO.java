package com.jins.aimentor.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePlanStatusDTO {

    /**
     * 新状态
     * <p>只能是"未完成"或"已完成"</p>
     */
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(未完成|已完成)$", message = "状态只能是'未完成'或'已完成'")
    private String status;
}