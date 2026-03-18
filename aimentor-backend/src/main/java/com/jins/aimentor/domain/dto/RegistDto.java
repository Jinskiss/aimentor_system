package com.jins.aimentor.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册表单
 */
@Data
public class RegistDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    @NotNull(message = "用户名不能为空")
    private String username;

    /**
     * 用户姓名
     */
    @NotNull(message = "用户姓名不能为空")
    private String name;

    /**
     * 密码
     */
    @NotNull(message = "密码不能为空")
    private String password;

    /**
     * 角色 student, teacher, admin
     */
    @NotNull(message = "角色不能为空")
    private String role;

    /**
     * 邮箱
     */
    @NotNull(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号
     */
    @NotNull(message = "手机号不能为空")
    private String phone;

}
