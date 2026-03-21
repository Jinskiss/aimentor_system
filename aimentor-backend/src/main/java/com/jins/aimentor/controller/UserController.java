package com.jins.aimentor.controller;

import com.jins.aimentor.common.Result;
import com.jins.aimentor.domain.dto.LoginDto;
import com.jins.aimentor.domain.dto.RegistDto;
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
}