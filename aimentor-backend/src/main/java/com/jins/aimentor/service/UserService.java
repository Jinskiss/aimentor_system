package com.jins.aimentor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jins.aimentor.domain.dto.LoginDto;
import com.jins.aimentor.domain.dto.RegistDto;
import com.jins.aimentor.domain.entity.User;
import com.jins.aimentor.domain.vo.UserVO;

public interface UserService extends IService<User> {
    /**
     * 注册用户
     * @param registForm
     * @return
     */
    User register(RegistDto registForm);


    /**
     * 登录
     * @param request
     * @return
     */
    String login(LoginDto request);

    /**
     * 获取当前用户信息
     * @param userId
     * @return
     */
    UserVO getUserInfoById(Long userId);

    /**
     * 更新用户信息（姓名/邮箱/手机/性别/年级/简介）
     * @param userId 用户ID
     * @param user 更新字段
     */
    void updateUserInfo(Long userId, User user);

    /**
     * 发送验证码
     * @param phone 手机号
     * @return 验证码
     */
    String sendVerificationCode(String phone);

    /**
     * 验证验证码是否正确
     * @param phone 手机号
     * @param code 用户输入的验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String phone, String code);
}
