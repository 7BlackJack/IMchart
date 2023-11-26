package com.imchat.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.imchat.server.entity.ChatRoom;
import com.imchat.server.service.ChatRoomService;
import com.imchat.server.mapper.ChatRoomMapper;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【chat_room】的数据库操作Service实现
* @createDate 2023-11-26 12:39:23
*/
@Service
public class ChatRoomServiceImpl extends ServiceImpl<ChatRoomMapper, ChatRoom>
    implements ChatRoomService{

}




