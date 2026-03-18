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

//    /**
//     * 通过userId查询用户信息
//     * @param page
//     * @param rows
//     * @param user
//     * @return
//     */
//    Page<User> pageList(int page, int rows, User user);
//
//    /**
//     * 通过userId查询用户信息
//     * @param user
//     * @param queryUserId
//     * @return
//     */
//    UserVO getUserInfo(User user, Long queryUserId);
//
//    /**
//     * 重置密码
//     * @param user
//     * @param newPassword
//     */
//    void resetPassword(User user, String newPassword);
//
//    /**
//     * 通过userId修改用户信息
//     * @param user
//     * @param updateUser
//     */
//    void updateUser(User user, User updateUser);
}
