package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.domain.dto.LoginDto;
import com.jins.aimentor.domain.dto.RegistDto;
import com.jins.aimentor.domain.dto.SendCodeDto;
import com.jins.aimentor.domain.entity.User;
import com.jins.aimentor.domain.vo.UserVO;
import com.jins.aimentor.service.UserService;
import com.jins.aimentor.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Api(tags = "用户管理", description = "提供用户信息的接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/test")
    public String test() {
        System.out.println("==== TestController.test() 被调用 ====");
        return "后端正常";
    }

    /**
     * 注册
     * @param registForm
     * @return 用户信息
     */
    @ApiOperation("用户注册接口")
    @PostMapping("/register")
    public Result<User> register(@Validated @RequestBody RegistDto registForm) {
        log.info("用户注册接口");

        if (registForm.getPassword() == null || registForm.getPassword().isEmpty()) {
            registForm.setPassword("123456");
        }

        return Result.success(userService.register(registForm));
    }

    /**
     * 发送验证码
     * @param dto 包含手机号
     * @return 验证码（模拟返回）
     */
    @ApiOperation("发送验证码")
    @PostMapping("/sendCode")
    public Result<String> sendCode(@RequestBody SendCodeDto dto) {
        log.info("发送验证码，手机号: {}", dto.getPhone());

        String code = userService.sendVerificationCode(dto.getPhone());
        return Result.success(code);
    }

    /**
     * 登录
     * @param request
     * @return
     */
    @ApiOperation("用户登录接口")
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDto request) {
        log.info("用户登录接口");

        String token = userService.login(request);

        return Result.success(token);
    }

    /**
     * 获取用户信息
     * @return
     */
    @ApiOperation("用户信息接口")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        // 从ThreadLocal中获取当前登录用户
        User user = UserHolder.getUser();

        if (user == null) {
            return Result.error("用户未登录");
        }

        log.info("获取当前用户信息，userId: {}", user.getId());
        UserVO userVO = userService.getUserInfoById(user.getId());

        return Result.success(userVO);
    }

    /**
     * 更新个人资料
     */
    @ApiOperation("更新个人资料")
    @PutMapping("/update")
    public Result<Void> updateUserInfo(@RequestBody User user) {
        User current = UserHolder.getUser();
        if (current == null) {
            return Result.error("用户未登录");
        }
        userService.updateUserInfo(current.getId(), user);
        return Result.success(null);
    }

    /**
     * 更新头像URL
     */
    @ApiOperation("更新头像URL")
    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@RequestBody java.util.Map<String, String> body) {
        User current = UserHolder.getUser();
        if (current == null) {
            return Result.error("用户未登录");
        }
        String avatarUrl = body.get("avatar");
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return Result.error("头像URL不能为空");
        }
        userService.updateAvatar(current.getId(), avatarUrl);
        log.info("[UserController] 更新头像，userId={}, avatar={}", current.getId(), avatarUrl);
        return Result.success(null);
    }

    /**
     * 发送邮箱验证码
     */
    @ApiOperation("发送邮箱验证码")
    @PostMapping("/sendEmailCode")
    public Result<String> sendEmailCode(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        log.info("发送邮箱验证码，邮箱: {}", email);

        String code = userService.sendEmailCode(email);
        return Result.success(code);
    }

    /**
     * 验证邮箱验证码并更新邮箱
     */
    @ApiOperation("验证邮箱验证码并更新邮箱")
    @PostMapping("/verifyEmailCode")
    public Result<Void> verifyEmailCode(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        String code = body.get("code");
        log.info("验证邮箱验证码，邮箱: {}", email);

        if (!userService.verifyEmailCode(email, code)) {
            return Result.error("验证码错误或已过期");
        }

        // 验证通过，更新用户邮箱
        User current = UserHolder.getUser();
        if (current == null) {
            return Result.error("用户未登录");
        }

        User updateUser = new User();
        updateUser.setEmail(email);
        userService.updateUserInfo(current.getId(), updateUser);

        return Result.success(null);
    }

    /**
     * 修改密码
     */
    @ApiOperation("修改密码")
    @PutMapping("/changePassword")
    public Result<Void> changePassword(@RequestBody java.util.Map<String, String> body) {
        User current = UserHolder.getUser();
        if (current == null) {
            return Result.error("用户未登录");
        }

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("旧密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        boolean success = userService.changePassword(current.getId(), oldPassword, newPassword);
        if (!success) {
            return Result.error("旧密码错误");
        }

        return Result.success(null);
    }
}