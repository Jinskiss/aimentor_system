package com.jins.aimentor.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 学生列表项 VO
 *
 * <p>用于教师端左侧学生列表展示
 *
 * <p>⚠️ 注意：id 字段使用 Long 类型，但通过 @JsonSerialize 注解在序列化时转为 String，
 * 避免前端 JavaScript Number 精度丢失问题（雪花算法生成的 ID 超过 16 位时，
 * JS Number 无法精确表示，会丢失末尾几位数字）。
 */
@Data
public class StudentListItemVO {

    /**
     * 学生 ID（Long 类型，序列化为字符串）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 年级
     */
    private String grade;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 该生平均成绩
     */
    private Double avgScore;

    /**
     * 薄弱知识点数量
     */
    private Integer weakPointCount;
}
