package com.jins.user.domain.form;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册表单
 */
@Data
public class RegistForm implements Serializable {

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
    private String email;

    /**
     * 手机号
     */
    private String phone;

}
