package com.jins.aimentor.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AskDTO {

    /**
     * 问题内容
     */
    @NotNull(message = "问题不能为空")
    @Size(max = 500, message = "问题长度不能超过500字")
    private String question;
}