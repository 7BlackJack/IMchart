package com.imchat.server.controller;

import cn.hutool.core.convert.ConverterRegistry;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imchat.server.entity.ChatMessage;
import com.imchat.server.entity.ChatUser;
import com.imchat.server.service.ChatMessageService;
import com.imchat.server.service.ChatUserService;
import com.imchat.server.util.ThreadLocalUtil;
import com.imchat.server.vo.MessageVo;
import com.imchat.server.vo.ResponseVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description 消息记录控制器
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/26
 */
@RestController
@Slf4j
@RequestMapping("message")
@RequiredArgsConstructor
public class MessageController {

    private final ChatMessageService chatMessageService;

    private final ChatUserService userService;



    /**
     * 发送消息 实际上是保存到数据库中
     * @param chatMessage
     * @return
     */
    @PostMapping("/send")
    public ResponseVo<String> sendMessage(@RequestBody ChatMessage chatMessage) {

        chatMessage.setCreateTime(new Date());
        chatMessageService.save(chatMessage);
        return ResponseVo.success("发送成功");
    }

    /**
     * 获取自己与某个用户的聊天记录
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseVo<List<MessageVo>> getUserMessage(@PathVariable String userId) {
        LambdaQueryWrapper<ChatMessage> lq = new LambdaQueryWrapper<>();
        lq.or(q -> q.eq(ChatMessage::getToId, userId).eq(ChatMessage::getUserId, ((ChatUser)ThreadLocalUtil.get()).getId()))
                        .or(q -> q.eq(ChatMessage::getUserId, userId).eq(ChatMessage::getToId, ((ChatUser)ThreadLocalUtil.get()).getId()))
                .eq(ChatMessage::getRoomId,0);
//        lq.eq(ChatMessage::getToId, userId);
//        lq.eq(ChatMessage::getUserId, ((ChatUser)ThreadLocalUtil.get()).getId());
        List<ChatMessage> list = chatMessageService.list(lq);
        List<MessageVo> target = new ArrayList<>();
        ConverterRegistry converterRegistry = ConverterRegistry.getInstance();
        list.forEach(item -> {
            MessageVo messageVo = converterRegistry.convert(MessageVo.class,item);
            // 获取用户名
            ChatUser user = userService.getById(item.getUserId());
            if (user != null) {
                messageVo.setUserName(user.getUsername());
            } else {
                messageVo.setUserName("已注销");
            }

            target.add(messageVo);
        });
        return ResponseVo.success(target);
    }

    /**
     * 获取指定房间所有聊天记录
     * @param roomId
     * @return
     */
    @GetMapping("/room/{roomId}")
    public ResponseVo<List<MessageVo>> getRoomMessage(@PathVariable String roomId) {

        // 判断会员逻辑，这里直接禁止非会员访问
        if (((ChatUser)ThreadLocalUtil.get()).getVip() == 0) {
            return ResponseVo.error(-1303,"无权限哦");
        }

        LambdaQueryWrapper<ChatMessage> lq = new LambdaQueryWrapper<>();
        lq.eq(ChatMessage::getRoomId, roomId);
        List<ChatMessage> list = chatMessageService.list(lq);
        List<MessageVo> target = new ArrayList<>();
        ConverterRegistry converterRegistry = ConverterRegistry.getInstance();
        list.forEach(item -> {
            MessageVo messageVo = converterRegistry.convert(MessageVo.class,item);
            // 获取用户名
            ChatUser user = userService.getById(item.getUserId());
            if (user != null) {
                messageVo.setUserName(user.getUsername());
            } else {
                messageVo.setUserName("已注销");
            }

            target.add(messageVo);
        });
        return ResponseVo.success(target);
    }
}
