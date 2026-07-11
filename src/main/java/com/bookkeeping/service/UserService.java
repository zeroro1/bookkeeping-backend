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
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    public Result<?> login(LoginDTO loginDTO) {
        String openid = getOpenidFromWechat(loginDTO.getCode());
        if (openid == null) {
            return Result.error(400, "微信登录失败");
        }

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

    private String getOpenidFromWechat(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=wx9db6d49c952450a5"
                + "&secret=f965372349fb541f694eabc6ce79a293"
                + "&js_code=" + code
                + "&grant_type=authorization_code";
        try {
            String resp = restTemplate.getForObject(url, String.class);
            Pattern p = Pattern.compile(""openid"\s*:\s*"([^"]+)"");
            Matcher m = p.matcher(resp);
            if (m.find()) {
                return m.group(1);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public record LoginResult(Long userId, String openid, String token) {}
}
