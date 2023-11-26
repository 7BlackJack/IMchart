package com.imchat.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imchat.server.entity.ChatRoomUser;
import com.imchat.server.service.ChatRoomUserService;
import com.imchat.server.mapper.ChatRoomUserMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【chat_room_user】的数据库操作Service实现
* @createDate 2023-11-26 12:39:23
*/
@Service
public class ChatRoomUserServiceImpl extends ServiceImpl<ChatRoomUserMapper, ChatRoomUser>
    implements ChatRoomUserService{

}




