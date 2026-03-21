package com.jins.aimentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jins.aimentor.constants.RedisConstants;
import com.jins.aimentor.constants.Status;
import com.jins.aimentor.domain.dto.LoginDto;
import com.jins.aimentor.domain.dto.RegistDto;
import com.jins.aimentor.domain.entity.User;
import com.jins.aimentor.domain.vo.UserVO;
import com.jins.aimentor.exception.BizException;
import com.jins.aimentor.mapper.UserMapper;
import com.jins.aimentor.service.UserService;
import com.jins.aimentor.utils.TokenUtils;
import com.jins.aimentor.utils.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final RedisTemplate redisTemplate;

    private final UserMapper userMapper;

    /**
     * 注册
     * @param registForm
     * @return
     */
    @Override
    @Transactional()
    public User register(RegistDto registForm) {
        log.info("用户注册，用户名: {}", registForm.getUsername());

        //查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registForm.getUsername());
        User user = getOne(queryWrapper);

        //用户已存在
        if (user != null) {
            log.error("用户注册失败，用户名已存在: {}", registForm.getUsername());
            throw new BizException(Status.CODE_403, "用户名已存在");
        }

        user = new User();
        user.setUsername(registForm.getUsername());
        user.setPassword(registForm.getPassword());
        user.setEmail(registForm.getEmail());
        user.setPhone(registForm.getPhone());
        user.setRole(registForm.getRole());
        save(user);

        return user;
    }

    /**
     * 登录
     *
     * @param loginForm 登录表单
     * @return 用户信息
     */
    @Override
    public String login(LoginDto loginForm) {
        log.info("用户登录，用户名: {}", loginForm.getUsername());

        //查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(StringUtils.isNotBlank(loginForm.getUsername()), User::getUsername, loginForm.getUsername())
                .eq(StringUtils.isNotBlank(loginForm.getPassword()), User::getPassword, loginForm.getPassword());
        User user = getOne(queryWrapper);

        //用户不存在
        if (user == null) {
            log.error("用户登录失败，用户名或密码错误，用户名: {}，密码：{}", loginForm.getUsername(), loginForm.getPassword());
            throw new BizException(Status.CODE_403, "用户名或密码错误");
        }

        //保存用户信息到ThreadLocal
        UserHolder.saveUser(user);

        //生成token
        String token = TokenUtils.genToken(user.getId().toString(), user.getUsername());
        log.info("用户登录成功，生成token，{}", token);

        //把用户存到redis中
        redisTemplate.opsForValue().set(RedisConstants.USER_TOKEN_KEY + token, user);

        //jwt不设置过期时间，只设置redis过期时间。
        redisTemplate.expire(RedisConstants.USER_TOKEN_KEY + token, RedisConstants.USER_TOKEN_TTL, TimeUnit.MINUTES);

        return token;
    }

    /**
     * 获取当前用户信息
     * @param userId
     * @return
     */
    @Override
    public UserVO getUserInfoById(Long userId) {
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 转换为 VO，隐藏密码等敏感信息
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());

        // 根据需要设置其他字段
        return userVO;
    }
}
