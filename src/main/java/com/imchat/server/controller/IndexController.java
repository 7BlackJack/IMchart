package com.imchat.server.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.imchat.server.dto.RegisterDTO;
import com.imchat.server.entity.ChatRoomUser;
import com.imchat.server.entity.ChatUser;
import com.imchat.server.service.ChatRoomUserService;
import com.imchat.server.service.ChatUserService;
import com.imchat.server.util.ThreadLocalUtil;
import com.imchat.server.vo.ResponseVo;
import com.imchat.server.websocket.WebsocketService;
import com.wf.captcha.GifCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/26
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class IndexController {

    private final ChatUserService userService;
    private final ChatRoomUserService chatRoomUserService;

    @RequestMapping("test")
    public String test() {
        return "test";
    }


    @GetMapping({"/","/index"})
    public String index(Model model) {
        ChatUser user = (ChatUser) ThreadLocalUtil.get();
        model.addAttribute("user",user.getUsername());
        model.addAttribute("user_id",user.getId());
        model.addAttribute("token",user.getLoginSign());

        // 获取已经加入群的群聊ID
        Set<Integer> roomIdList = chatRoomUserService.list(Wrappers.<ChatRoomUser>lambdaQuery().eq(ChatRoomUser::getUserId, user.getId()))
                .stream().map(ChatRoomUser::getRoomId).collect(Collectors.toSet());

        model.addAttribute("my_room_list", roomIdList);
        return "page/index";
    }

    /**
     * 获取在线人数
     * @return
     */
    @GetMapping("/getOnlineNums")
    @ResponseBody
    public ResponseVo<Integer> getOnlineNums() {
        return ResponseVo.success(WebsocketService.getOnlineNum());
    }

    /**
     * 登录页
     * @return
     */
    @GetMapping("/login")
    public String login() {
        return "page/login";
    }

    /**
     * 用户登录请求
     * @param registerDTO
     * @return
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseVo<Map<String,String>> login(@RequestBody RegisterDTO registerDTO,HttpServletResponse response) {

        LambdaQueryWrapper<ChatUser> lq = new LambdaQueryWrapper<>();
        lq.eq(ChatUser::getUsername,registerDTO.getUsername());
        ChatUser user = userService.getOne(lq);
        if (user == null) {
            return ResponseVo.error(-1103,"该用户不存在");
        }
        if (!user.getPassword().equals(registerDTO.getPassword())) {
            return ResponseVo.error(-1102,"登录失败，账号或密码错误");
        }
        if (user.getStatus() != 1) {
            return ResponseVo.error(-1105,"登录失败，账号已被封禁");
        }
        // 登录成功 存储信息
        // 生成一个token，存到
        String token = SecureUtil.md5(registerDTO.getUsername() + System.currentTimeMillis());
        user.setLoginSign(token);
        userService.updateById(user);
        Map<String, String> map = new HashMap<>(1);
        map.put("token",token);
        Cookie cookie = new Cookie("login_token", token);
        cookie.setMaxAge(86400); // 设置 cookie 的过期时间，单位为秒
        cookie.setPath("/"); // 设置 cookie 的作用路径，这里设置为根路径，表示在整个网站内有效
        response.addCookie(cookie); // 添加cookie到当前响应头
        return ResponseVo.success("登录成功",map);
    }

    /**
     * 注册请求
     * @param registerDTO
     * @return
     */
    @PostMapping("/register")
    @ResponseBody
    public ResponseVo<String> register(@RequestBody RegisterDTO registerDTO,HttpServletRequest request) {
        log.info("用户名：{}\n密码：{}\n验证码：{}",registerDTO.getUsername(),registerDTO.getPassword(),registerDTO.getCaptcha());
        // 判断验证码
        String captcha = registerDTO.getCaptcha();
        String verifyCode = (String) request.getSession().getAttribute("verifyCode");
        request.getSession().removeAttribute("verifyCode");
        if (StrUtil.isBlank(verifyCode)) {
            return ResponseVo.error(-1,"验证码失效，请刷新验证码再试");
        }

        log.info("提交的验证码：{},真实验证码：{}",captcha,verifyCode);

        if (!verifyCode.equalsIgnoreCase(captcha)) {
           return ResponseVo.error(-2,"验证码错误");
        }

        // 判断用户名是否已经存在
        if (StrUtil.isBlank(registerDTO.getUsername()) || StrUtil.isBlank(registerDTO.getPassword())) {
            return ResponseVo.error(-3,"用户名和密码不能留空哦");
        }
        LambdaQueryWrapper<ChatUser> lq = new LambdaQueryWrapper<>();
        lq.eq(ChatUser::getUsername,registerDTO.getUsername());
        if (userService.getOne(lq) != null) {
            return ResponseVo.error(-4,"该用户名已被注册了");
        }
        // 可以注册
        ChatUser chatUser = new ChatUser();
        chatUser.setUsername(registerDTO.getUsername());
        chatUser.setPassword(registerDTO.getPassword());
        chatUser.setVip(0);
        boolean res = userService.save(chatUser);
        if (res) {
            return ResponseVo.success("注册成功");
        }

        return ResponseVo.error(-1011,"注册失败");
    }

    /**
     * 注册页
     * @return
     */
    @GetMapping("/register")
    public String register(){
        return "page/register";
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control","no-store");
        response.setHeader("Pragma","no-cache");
        response.setDateHeader("Expires",0);
        response.setContentType("image/gif");
        //生成验证码对象,三个参数分别是 宽、高、位数
        GifCaptcha captcha = new GifCaptcha(130, 32, 5);
        // 设置验证码的字符类型为数字和字母混合
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        // 设置内置字体
        captcha.setCharType(Captcha.FONT_1);
        // 验证码存入session
        request.getSession().setAttribute("verifyCode",captcha.text().toLowerCase());
        //输出图片流
        captcha.out(response.getOutputStream());
    }
}
