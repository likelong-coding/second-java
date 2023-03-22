package com.hmdp.interceptor;

import com.hmdp.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器
 *
 * @author likelong
 * @date 2023/3/22
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断ThreadLocal中是否有用户信息
        if (UserHolder.getUser() == null) {
            // 设置状态码
            response.setStatus(401);
            // 拦截
            return false;
        }
        // 放行
        return true;
    }
}
