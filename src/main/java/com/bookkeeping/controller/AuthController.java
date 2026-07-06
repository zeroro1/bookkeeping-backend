package com.bookkeeping.controller;

import com.bookkeeping.common.Result;
import com.bookkeeping.dto.LoginDTO;
import com.bookkeeping.entity.User;
import com.bookkeeping.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /** 微信登录 */
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    /** 测试登录（开发用） */
    @GetMapping("/test-login")
    public Result<?> testLogin() {
        return userService.testLogin();
    }

    /** 获取用户信息 */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String token) {
        String openid = userService.getOpenIdFromToken(token.replace("Bearer ", ""));
        if (openid == null) {
            return Result.error(401, "未登录");
        }
        User user = userService.getUserByOpenid(openid);
        return Result.success(user);
    }
}