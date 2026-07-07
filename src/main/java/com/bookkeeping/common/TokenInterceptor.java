package com.bookkeeping.common;

import com.bookkeeping.entity.User;
import com.bookkeeping.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{"code":401,"message":"未授权"}");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{"code":401,"message":"令牌无效"}");
            return false;
        }

        // 从 token 中获取 openid
        String openid = jwtUtil.getOpenidFromToken(token);
        // 根据 openid 查找用户获取 userId
        User user = userService.getUserByOpenid(openid);
        if (user == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{"code":401,"message":"用户不存在"}");
            return false;
        }

        // 将 userId 存入 request，Controller 可直接获取
        request.setAttribute("userId", user.getId());
        return true;
    }
}
