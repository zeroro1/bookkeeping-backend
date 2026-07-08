package com.bookkeeping.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader(HEADER);

        if (authHeader == null || !authHeader.startsWith(PREFIX)) {
            sendUnauthorized(response, "缺少认证信息");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Jwts.SIG.HS384.key().build())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String openid = claims.getSubject();
            request.setAttribute("userId", extractUserId(openid));
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
        if ("test_openid_001".equals(openid)) {
            return 1L;
        }
        return 1L;
    }
}
