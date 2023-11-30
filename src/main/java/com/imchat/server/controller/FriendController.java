package com.imchat.server.controller;

import cn.hutool.core.convert.ConverterRegistry;
import com.imchat.server.entity.ChatUser;
import com.imchat.server.service.ChatUserService;
import com.imchat.server.util.ThreadLocalUtil;
import com.imchat.server.vo.ResponseVo;
import com.imchat.server.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Description 私聊控制器
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/26
 */
@Controller
@Slf4j
@RequestMapping("/friend/")
@RequiredArgsConstructor
public class FriendController {

    private final ChatUserService chatUserService;

    /**
     * 获取用户列表
     * @return
     */
    @GetMapping("list")
    @ResponseBody
    public ResponseVo<List<UserVo>> getList() {
        List<ChatUser> list = chatUserService.list();
        List<UserVo> target = new ArrayList<>();
        ConverterRegistry converterRegistry = ConverterRegistry.getInstance();
        list.forEach(item -> {
            // 如果是自己，跳过
            if (Objects.equals(item.getId(), ((ChatUser) ThreadLocalUtil.get()).getId())) {
                return;
            }
            UserVo userVo = converterRegistry.convert(UserVo.class, item);

            target.add(userVo);
        });

        return ResponseVo.success(target);
    }
}
