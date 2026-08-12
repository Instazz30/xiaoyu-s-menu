package com.xiaxiaoyu.xingbangmenu.config;

import com.xiaxiaoyu.xingbangmenu.entity.SysUser;
import com.xiaxiaoyu.xingbangmenu.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            SysUser user = authService.getByToken(header.substring(7));
            if (user != null) {
                request.setAttribute("currentUser", user);
                // 返回服务端实际识别的用户，前端据此检测 token 与本地用户缓存是否串号。
                response.setHeader("X-User-Id", String.valueOf(user.getId()));
                return true;
            }
        }

        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\"}");
        return false;
    }
}
