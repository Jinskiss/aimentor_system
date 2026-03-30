package com.jins.aimentor.domain.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 成绩记录添加/编辑表单
 */
@Data
public class ScoreRecordDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID（编辑时需要）
     */
    private Long id;

    /**
     * 考试日期
     */
    @NotNull(message = "考试日期不能为空")
    private LocalDate examDate;

    /**
     * 科目名称
     */
    @NotBlank(message = "科目名称不能为空")
    private String subject;

    /**
     * 考试得分
     */
    @NotNull(message = "考试得分不能为空")
    @Min(value = 0, message = "分数不能为负数")
    @Max(value = 1000, message = "分数不能超过1000")
    private Integer score;

    /**
     * 满分分值
     */
    @NotNull(message = "满分分值不能为空")
    @Min(value = 1, message = "满分至少为1")
    private Integer fullScore;
}
