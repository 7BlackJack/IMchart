package com.imchat.server.controller;

import cn.hutool.core.convert.ConverterRegistry;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imchat.server.dto.RoomDTO;
import com.imchat.server.entity.ChatRoom;
import com.imchat.server.entity.ChatRoomUser;
import com.imchat.server.entity.ChatUser;
import com.imchat.server.service.ChatRoomService;
import com.imchat.server.service.ChatRoomUserService;
import com.imchat.server.service.ChatUserService;
import com.imchat.server.util.ThreadLocalUtil;
import com.imchat.server.vo.ResponseVo;
import com.imchat.server.vo.RoomVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description 房间控制器
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/26
 */
@Controller
@Slf4j
@RequestMapping("/room/")
@RequiredArgsConstructor
public class RoomController {

    private final ChatRoomService chatRoomService;
    private final ChatRoomUserService chatRoomUserService;
    private final ChatUserService userService;

    /**
     * 创建房间
     * @return
     */
    @PostMapping("create")
    @ResponseBody
    public ResponseVo<String> createRoom(@RequestBody RoomDTO roomDTO) {

        ChatRoom chatRoom = new ChatRoom();

        chatRoom.setCreateTime(new Date());
        chatRoom.setUserId(((ChatUser)ThreadLocalUtil.get()).getId());
        chatRoom.setPass("123456");
        chatRoom.setLimitNum(roomDTO.getLimitNum() < 2 ? 2 : roomDTO.getLimitNum());
        chatRoom.setName(roomDTO.getName());

        chatRoomService.save(chatRoom);
        // 默认进入房间
        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setRoomId(chatRoom.getId());
        chatRoomUser.setUserId(((ChatUser)ThreadLocalUtil.get()).getId());
        chatRoomUser.setCreateTime(new Date());
        chatRoomUserService.save(chatRoomUser);

        return ResponseVo.success("创建成功");
    }

    /**
     * 退出群聊
     * @param roomId
     * @return
     */
    @PostMapping("quit")
    @ResponseBody
    public ResponseVo<String> quitRoom(@RequestParam("room_id") Integer roomId) {
        // 判断是否在这个房间
        LambdaQueryWrapper<ChatRoomUser> lq = new LambdaQueryWrapper<>();
        lq.eq(ChatRoomUser::getRoomId,roomId);
        lq.eq(ChatRoomUser::getUserId,((ChatUser)ThreadLocalUtil.get()).getId());
        ChatRoomUser chatRoomUser = chatRoomUserService.getOne(lq);
        if (chatRoomUser == null) {
            return ResponseVo.error(-1203,"退出失败，你不在房间里");
        }

        chatRoomUserService.removeById(chatRoomUser);

        return ResponseVo.success("退出群聊成功");
    }

    /**
     * 加入群聊
     * @return
     */
    @PostMapping("join")
    @ResponseBody
    public ResponseVo<String> joinRoom(@RequestParam("room_id") Integer roomId) {

        // 判断房间是否存在
        ChatRoom room = chatRoomService.getById(roomId);
        if (null == room) {
            return ResponseVo.error(-1201,"房间不存在");
        }

        // 判断是否满了
        LambdaQueryWrapper<ChatRoomUser> lq = new LambdaQueryWrapper<>();
        lq.eq(ChatRoomUser::getRoomId,roomId);
        long count = chatRoomUserService.count(lq);
        if (count >= room.getLimitNum()) {
            return ResponseVo.error(-1202,"房间人满了");
        }
        // 判断是否已经存在房间里
        lq.eq(ChatRoomUser::getUserId,((ChatUser) ThreadLocalUtil.get()).getId());
        if (chatRoomUserService.count(lq) > 0) {
            return ResponseVo.error(-1203,"你已经在这房间里了哦");
        }
        // 可以加入
        ChatRoomUser chatRoomUser = new ChatRoomUser();
        chatRoomUser.setRoomId(roomId);
        chatRoomUser.setUserId(((ChatUser)ThreadLocalUtil.get()).getId());
        chatRoomUser.setCreateTime(new Date());
        chatRoomUserService.save(chatRoomUser);
        System.out.println(chatRoomUser);
        return ResponseVo.success("加入成功");
    }

    /**
     * 获取所有房间列表
     * @return
     */
    @GetMapping("list")
    @ResponseBody
    public ResponseVo<List<RoomVo>> getList() {
        List<ChatRoom> list = chatRoomService.list();
        List<RoomVo> target = new ArrayList<>();
        ConverterRegistry converterRegistry = ConverterRegistry.getInstance();
        list.forEach(item -> {
            RoomVo roomVo = converterRegistry.convert(RoomVo.class, item);
            // 获取房主名称
            ChatUser masterUser = userService.getById(item.getUserId());
            if (masterUser == null) {
                // 这个房间跳过，并且删除
                chatRoomService.removeById(item.getId());
                return;
            }
            roomVo.setMasterName(masterUser.getUsername());
            target.add(roomVo);
        });
        System.out.println(target);
        return ResponseVo.success(target);
    }
}
