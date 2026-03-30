package com.jins.aimentor.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送验证码请求DTO
 */
@Data
public class SendCodeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    private String phone;

}