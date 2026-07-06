package com.bookkeeping.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bookkeeping.common.JwtUtil;
import com.bookkeeping.common.Result;
import com.bookkeeping.dto.LoginDTO;
import com.bookkeeping.entity.User;
import com.bookkeeping.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final JwtUtil jwtUtil;

    public Result<?> login(LoginDTO loginDTO) {
        String openid = simulateOpenid(loginDTO.getCode());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = getOne(wrapper);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户");
            save(user);
        }
        String token = jwtUtil.generateToken(openid);
        return Result.success(new LoginResult(user.getId(), openid, token));
    }

    public Result<?> testLogin() {
        String openid = "test_openid_001";
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = getOne(wrapper);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname("测试用户");
            save(user);
        }
        String token = jwtUtil.generateToken(openid);
        return Result.success(new LoginResult(user.getId(), openid, token));
    }

    public User getUserByOpenid(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        return getOne(wrapper);
    }

    public String getOpenIdFromToken(String token) {
        return jwtUtil.getOpenidFromToken(token);
    }

    private String simulateOpenid(String code) {
        if ("test".equals(code)) {
            return "test_openid_001";
        }
        return "wx_" + code;
    }

    public record LoginResult(Long userId, String openid, String token) {}
}
