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
}
