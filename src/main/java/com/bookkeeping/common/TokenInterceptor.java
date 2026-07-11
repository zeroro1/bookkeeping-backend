package com.bookkeeping.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookkeeping.entity.User;
import com.bookkeeping.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Key;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final UserMapper userMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader(HEADER);

        if (authHeader == null || !authHeader.startsWith(PREFIX)) {
            sendUnauthorized(response, "缺少认证信息");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            Key signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String openid = claims.getSubject();
            Long userId = extractUserId(openid);
            if (userId == null) {
                sendUnauthorized(response, "用户不存在");
                return false;
            }
            request.setAttribute("userId", userId);
            return true;
        } catch (Exception e) {
            sendUnauthorized(response, "令牌无效");
            return false;
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("code", 401);
        body.put("message", message);
        response.getWriter().write(mapper.writeValueAsString(body));
    }

    private Long extractUserId(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        User user = userMapper.selectOne(wrapper);
        return user != null ? user.getId() : null;
    }
}
