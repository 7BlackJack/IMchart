package com.imchat.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imchat.server.entity.ChatMessage;
import com.imchat.server.service.ChatMessageService;
import com.imchat.server.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【chat_message】的数据库操作Service实现
* @createDate 2023-11-26 12:39:23
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

}




