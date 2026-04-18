package com.jins.aimentor.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 学习计划视图对象
 *
 * <p>⚠️ 注意：id 字段使用 Long 类型，但通过 @JsonSerialize 注解在序列化时转为 String，
 * 避免前端 JavaScript Number 精度丢失问题。
 */
@Data
public class PlanVO {

    /**
     * 计划ID（Long 类型，序列化为字符串）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 计划内容
     */
    private String content;

    /**
     * 开始日期
     * <p>格式：yyyy-MM-dd</p>
     */
    private String startDate;

    /**
     * 结束日期
     * <p>格式：yyyy-MM-dd</p>
     */
    private String endDate;

    /**
     * 计划状态
     * <p>未完成 / 已完成</p>
     */
    private String status;
}
