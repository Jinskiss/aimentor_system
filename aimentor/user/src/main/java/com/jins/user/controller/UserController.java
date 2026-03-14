package com.jins.user.controller;

import com.jins.common.Result;
import com.jins.constants.Status;
import com.jins.user.domain.entity.User;
import com.jins.user.domain.form.LoginForm;
import com.jins.user.domain.form.RegistForm;
import com.jins.user.domain.vo.UserVO;
import com.jins.user.service.UserService;
import com.jins.user.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping
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
    @PostMapping("/user/register")
    public Result<User> register(@Validated @RequestBody RegistForm registForm) {
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
    @PostMapping("/user/login")
    public Result<String> login(@RequestBody LoginForm request) {
        log.info("用户登录接口");

        String token = userService.login(request);

        return Result.success(token);
    }

    @GetMapping("/user/info")
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

//
//    /**
//     * 分页查询用户列表
//     * @param page
//     * @param rows
//     * @return
//     */
//    @ApiOperation("分页查询用户列表")
//    @GetMapping("/users")
//    public R<Page<UserVO>> listUsers(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int rows) {
//        log.info("分页查询用户列表");
//
//        User user1 = UserHolder.getUser();
//
//        if (user1 == null) {
//            return R.error(Status.CODE_500, "用户未登录");
//        }
//
//        Page<User> userPage = userService.pageList(page, rows, user1);
//
//        List<UserVO> userVOList = new ArrayList<>();
//        if (!userPage.getRecords().isEmpty()) {
//            for (User user : userPage.getRecords()) {
//                UserVO userVO = new UserVO();
//                userVO.setUserId(user.getUserId());
//                userVO.setUsername(user.getUsername());
//                userVO.setEmail(user.getEmail());
//                userVO.setPhone(user.getPhone());
//                userVOList.add(userVO);
//            }
//        }
//
//        Page<UserVO> userVOPage = new Page<>(page, rows);
//        userVOPage.setRecords(userVOList);
//        userVOPage.setTotal(userPage.getTotal());
//        userVOPage.setCurrent(userPage.getCurrent());
//
//        return R.success(userVOPage);
//    }
//
//    /**
//     * 通过userId查询用户信息
//     * @param userId
//     * @return
//     */
//    @ApiOperation("通过userId查询用户信息")
//    @GetMapping("/user/info")
//    public Result<UserVO> getUserInfo(@PathVariable Long userId) {
//        log.info("通过userId查询用户信息");
//
//        User user = UserHolder.getUser();
//
//        if (user == null) {
//            return Result.error(Status.CODE_500, "用户未登录");
//        }
//
//        UserVO userVO = userService.getUserInfo(user, userId);
//
//        return Result.success(userVO);
//    }

//    /**
//     * 修改用户信息
//     * @param updateUser
//     * @return
//     */
//    @ApiOperation("修改用户信息")
//    @PutMapping("/updateUser")
//    public R updateUser(@RequestBody User updateUser) {
//        log.info("修改用户信息");
//
//        User user = UserHolder.getUser();
//
//        if (user == null) {
//            return R.error(Status.CODE_500, "用户未登录");
//        }
//
//        userService.updateUser(user, updateUser);
//
//        return R.success();
//    }
//
//    /**
//     * 重置密码
//     * @param newPassword
//     * @return
//     */
//    @ApiOperation("重置密码")
//    @PostMapping("/reset-password")
//    public R resetPassword(
//            @RequestParam String newPassword) {
//        log.info("重置密码");
//
//        User user = UserHolder.getUser();
//
//        if (user == null) {
//            return R.error(Status.CODE_500, "用户未登录");
//        }
//
//        userService.resetPassword(user, newPassword);
//
//        return R.success();
//    }
}