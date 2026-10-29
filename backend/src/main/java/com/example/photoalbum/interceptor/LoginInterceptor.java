//实现HandlerInterceptor
package com.example.photoalbum.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果是浏览器的 OPTIONS 预检请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        HttpSession session = request.getSession();
        Object loginUser = session.getAttribute("loginUser");

        // 如果 Session 里没用户，说明没登录
        if (loginUser == null) {
            // 返回 401 状态码和 JSON 格式提示
            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录，请先登录\",\"data\":null}");
            return false;
        }
        // 有登录用户，放行
        return true;
    }
}