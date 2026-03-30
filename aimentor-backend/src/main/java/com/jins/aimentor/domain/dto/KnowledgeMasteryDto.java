package com.jins.aimentor.domain.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 知识点掌握度添加/编辑表单
 */
@Data
public class KnowledgeMasteryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID（编辑时需要）
     */
    private Long id;

    /**
     * 知识点名称
     */
    @NotBlank(message = "知识点名称不能为空")
    private String knowledge;

    /**
     * 掌握度百分比
     */
    @NotNull(message = "掌握度不能为空")
    @Min(value = 0, message = "掌握度不能为负数")
    @Max(value = 100, message = "掌握度不能超过100")
    private Integer mastery;

    /**
     * 所属科目（可选，建议填写便于统计）
     */
    private String subject;
}
