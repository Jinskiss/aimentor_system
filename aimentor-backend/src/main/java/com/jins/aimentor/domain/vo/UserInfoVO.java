package com.jins.aimentor.domain.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * 用户信息VO
 * 返回给前端的用户信息（不包含敏感字段）
 *
 * <p>⚠️ 注意：id 字段使用 Long 类型，但通过 @JsonSerialize 注解在序列化时转为 String，
 * 避免前端 JavaScript Number 精度丢失问题。
 */
@Data
public class UserInfoVO {

    /** 用户ID（Long 类型，序列化为字符串） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 真实姓名 */
    private String name;

    /** 邮箱 */
    private String email;
}
