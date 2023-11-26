package com.imchat.server.handle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imchat.server.entity.ChatUser;
import com.imchat.server.service.ChatUserService;
import com.imchat.server.util.ThreadLocalUtil;
import com.imchat.server.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/26
 */
@Component
public class LoginHandleInterceptor implements HandlerInterceptor {

    @Autowired
    private ChatUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.sendRedirect("/login");
            return false;
//            response.setContentType("text/json; charset=utf-8");
//            response.setCharacterEncoding("utf-8");
//            response.getWriter().println(new ObjectMapper().writeValueAsString(ResponseVo.error(403,"未登录")));
//            return false;
        }
        String token = "";
        for (Cookie cookie : cookies) {
            if ("login_token".equals(cookie.getName())) {
                token = cookie.getValue();
                break;
            }
        }
        // 判断是否已经登录了
        LambdaQueryWrapper<ChatUser> lq = new LambdaQueryWrapper<>();
        lq.eq(ChatUser::getLoginSign,token);
        ChatUser user = userService.getOne(lq);
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }
        // 保存到线程中
        ThreadLocalUtil.add(user);
        return true;
    }
}
