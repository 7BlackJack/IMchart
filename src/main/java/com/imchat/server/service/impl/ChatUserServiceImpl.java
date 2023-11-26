package com.imchat.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imchat.server.entity.ChatUser;
import com.imchat.server.service.ChatUserService;
import com.imchat.server.mapper.ChatUserMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【chat_user】的数据库操作Service实现
* @createDate 2023-11-26 12:39:23
*/
@Service
public class ChatUserServiceImpl extends ServiceImpl<ChatUserMapper, ChatUser>
    implements ChatUserService{

}




