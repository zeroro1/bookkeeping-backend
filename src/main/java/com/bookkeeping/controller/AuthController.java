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

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @GetMapping("/test-login")
    public Result<?> testLogin() {
        return userService.testLogin();
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        String openid = userService.getOpenIdFromToken(token);
        if (openid == null) {
            return Result.error(401, "未登录");
        }
        User user = userService.getUserByOpenid(openid);
        return Result.success(user);
    }
}
