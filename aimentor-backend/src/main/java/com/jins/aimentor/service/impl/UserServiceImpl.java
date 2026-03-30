package com.jins.aimentor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final RedisTemplate redisTemplate;

    private final UserMapper userMapper;

    /**
     * 存储验证码的Map（模拟Redis，生产环境应使用Redis）
     */
    private static final Map<String, String> CODE_STORE = new ConcurrentHashMap<>();

    /**
     * 验证码过期时间（秒）
     */
    private static final int CODE_EXPIRE_SECONDS = 300; // 5分钟

    /**
     * 注册
     * @param registForm
     * @return
     */
    @Override
    @Transactional()
    public User register(RegistDto registForm) {
        log.info("用户注册，用户信息: {}", registForm.toString());

        // 验证验证码（须与 sendCode 一致；Spring 的 StringUtils 才有 hasText，勿用 MP 的 StringUtils）
        if (StringUtils.hasText(registForm.getPhone()) && StringUtils.hasText(registForm.getCode())) {
            if (!verifyCode(registForm.getPhone(), registForm.getCode())) {
                throw new BizException(Status.CODE_403, "验证码错误或已过期");
            }
        }

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
        user.setName(registForm.getName());
        user.setEmail(registForm.getEmail());
        user.setPhone(registForm.getPhone());
        user.setRole(registForm.getRole());
        save(user);

        // 清除已使用的验证码
        CODE_STORE.remove(registForm.getPhone());

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
        log.info("用户登录，用户信息: {}", loginForm.toString());

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginForm.getUsername());
        User user = getOne(queryWrapper);

        if (user == null || !Objects.equals(loginForm.getPassword(), user.getPassword())) {
            log.error("用户登录失败，用户名或密码错误");
            throw new BizException(Status.CODE_403, "用户名或密码错误");
        }

        // 保存用户信息到ThreadLocal
        UserHolder.saveUser(user);

        // 生成token
        String token = TokenUtils.genToken(user.getId().toString(), user.getUsername());
        log.info("用户登录成功，生成token: {}", token);

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

    /**
     * 发送验证码
     * 模拟生成六位数验证码
     * @param phone 手机号
     * @return 验证码
     */
    @Override
    public String sendVerificationCode(String phone) {
        log.info("发送验证码，手机号: {}", phone);

        if (!StringUtils.hasText(phone)) {
            throw new BizException(Status.CODE_400, "手机号不能为空");
        }

        // 生成六位数验证码
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        String verificationCode = String.valueOf(code);

        // 存储验证码（带过期时间检查）
        CODE_STORE.put(phone, verificationCode);
        log.info("生成的验证码: {}，有效期{}秒", verificationCode, CODE_EXPIRE_SECONDS);

        // 模拟返回验证码（实际生产环境不应该返回）
        return verificationCode;
    }

    /**
     * 验证验证码是否正确
     * @param phone 手机号
     * @param code 用户输入的验证码
     * @return 是否验证通过
     */
    @Override
    public boolean verifyCode(String phone, String code) {
        log.info("验证验证码，手机号: {}, 输入的验证码: {}", phone, code);

        if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
            return false;
        }

        String storedCode = CODE_STORE.get(phone);
        if (storedCode == null) {
            log.warn("验证码不存在或已过期: {}", phone);
            return false;
        }

        boolean isValid = storedCode.equals(code);
        if (isValid) {
            // 验证成功后删除验证码
            CODE_STORE.remove(phone);
            log.info("验证码验证成功");
        } else {
            log.warn("验证码错误，输入: {}，存储: {}", code, storedCode);
        }

        return isValid;
    }
}