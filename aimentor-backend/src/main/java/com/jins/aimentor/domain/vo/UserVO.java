package com.jins.aimentor.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * UserVO — 用户信息返回 VO（View Object）
 *
 * 用于登录后前端展示用户基本信息，以及在学情接口中作为 userId 的载体。
 *
 * 重要说明 — id 字段使用 ToStringSerializer 序列化：
 *   后端 User 实体的主键使用 MyBatis-Plus 的 IdType.ASSIGN_ID（雪花算法），
 *   生成的是 19 位 Long 类型整数（如 2036128867076329473）。
 *   前端 JavaScript 的 number 类型安全整数上限为 Number.MAX_SAFE_INTEGER（9e15），
 *   当雪花 ID 超过此范围时，JSON 解析会把末尾几位变成 0，导致 ID 不一致。
 *
 *   解决方案：对 id 字段使用 @JsonSerialize(using = ToStringSerializer.class)
 *   让 Jackson 在序列化时将 Long 转成 String（如 "2036128867076329473"），
 *   前端收到的是字符串，不会触发精度丢失。后续接口中 id 由后端从
 *   UserHolder（ThreadLocal）直接获取，彻底规避此问题。
 */
@Data
public class UserVO {

    /**
     * 用户ID（Long → String 序列化，防止 JS number 精度丢失）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String name;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 性别
     */
    private String gender;

    /**
     * 年级
     */
    private String grade;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 角色
     */
    private String role;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
