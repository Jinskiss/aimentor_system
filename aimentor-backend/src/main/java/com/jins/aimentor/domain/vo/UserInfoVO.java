package com.jins.aimentor.domain.vo;

import lombok.Data;

/**
 * 用户信息VO
 * 返回给前端的用户信息（不包含敏感字段）
 */
@Data
public class UserInfoVO {
    
    /** 用户ID */
    private Long id;
    
    /** 真实姓名 */
    private String name;
    
    /** 邮箱 */
    private String email;
}
