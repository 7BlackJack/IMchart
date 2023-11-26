package com.imchat.server.websocket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imchat.server.entity.ChatUser;
import com.imchat.server.service.ChatUserService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/25
 */

@ServerEndpoint(value = "/im/{token}")
@Component
@Slf4j
@Data
public class WebsocketService {
    /** 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。 **/
    private static AtomicInteger onlineNum = new AtomicInteger();

    /** concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketServer对象。**/
    private static ConcurrentHashMap<Integer, Session> sessionPools = new ConcurrentHashMap<>();

    /**
     * 用来储存房间
     * 房号 用户角色ID（不能重复）
     */
    private static ConcurrentHashMap<Integer, Set<Integer>> roomList = new ConcurrentHashMap<>();

    /** 与某个客户端的连接会话，需要通过它来给客户端发送数据 **/
    private Session session;

    /** 心跳报文 **/
    private static final String HEARTBEAT_PACKETS = "hearting";

    private static ChatUserService userService;
    private String token;
    private ChatUser currentUser;

    @Autowired
    private void setChatUserService(ChatUserService service) {
        userService = service;
    }

    /**
     * 群发
     */
    public void broadcast(Integer roomId, String message) throws IOException {
        // 判断这个群是否存在
        if (!roomList.containsKey(roomId)) {
            return;
        }
        // 群存在
        Set<Integer> members = roomList.get(roomId);
        for (Integer member : members) {
            // 群发消息
            sendMessage(sessionPools.get(member),message);
        }
    }

    /**
     * 发送消息
     * @param session 会话
     * @param message 消息
     * @throws IOException
     */
    public void sendMessage(Session session, String message) throws IOException {
        if(session != null){
            synchronized (session) {
                System.out.println("发送数据：" + message);
                session.getBasicRemote().sendText(message);
            }
        }
    }

    /**
     * 给指定用户发送消息
     * @param userId 用户Id
     * @param message 消息
     */
    public void sendInfo(Integer userId, String message){
        Session session = sessionPools.get(userId);
        try {
            sendMessage(session, message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 用户上线（登录）时调用
     * @param session
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "token") String token) throws IOException {
        // 判断是否登录
        if (!authenticate(token)) {
            session.getBasicRemote().sendText("未认证");
            session.close();
            return;
        }
        this.token = token;
        this.session = session;
        sessionPools.put(this.currentUser.getId(), session);
        addOnlineCount();
        System.out.println(this.currentUser.getUsername() + "加入服务器！当前人数为" + onlineNum);
    }

    /**
     * 用户下线（退出）时调用
     */
    @OnClose
    public void onClose(){
        // 只有存在的时候才移除
        if (this.currentUser != null) {
            sessionPools.remove(this.currentUser.getId());
            subOnlineCount();
            System.out.println(this.currentUser.getUsername() + "断开服务器连接！当前人数为" + onlineNum);
        }
    }

    /**
     * 收到客户端消息后调用 根据接收人的username把消息推下去或者群发
     * 如果to=-1 那就是群发
     * @param message
     * @throws IOException
     */
    @OnMessage
    public void onMessage(String message) throws IOException{
        if (HEARTBEAT_PACKETS.equals(message)) {
            log.debug("[消息订阅] - 心跳.");
            return;
        }
        // 判断是否登录
        if (!authenticate(this.token)) {
            session.getBasicRemote().sendText("认证失效了");
            session.close();
            return;
        }
        /**
         * 把消息处理
         */
        ObjectMapper objectMapper = new ObjectMapper();
        WebsocketMessageVo msg = objectMapper.readValue(message, WebsocketMessageVo.class);
        switch (msg.getAction()) {
            case 0:
                // 发送消息
                msg.setDate(new Date());
                if (msg.getRoomId() > 0) {
                    // 群发
                    broadcast(msg.getRoomId(), objectMapper.writeValueAsString(msg));
                } else {
                    sendInfo(msg.getToId(), objectMapper.writeValueAsString(msg));
                }
                break;
            case 1:
                // 进入群聊
                Integer roomId = msg.getRoomId();
                if (roomId <= 0) {
                    msg.setToId(this.currentUser.getId());
                    msg.setUserId(0);
                    msg.setContent("房间号非法");
                    msg.setDate(new Date());
                    session.getBasicRemote().sendText(objectMapper.writeValueAsString(msg));
                    return;
                }
                String tips = "加入";
                // 如果这个群聊不存在，那么就创建
                if (!roomList.containsKey(roomId)) {
                    Set<Integer> integers = new HashSet<>();
                    integers.add(this.currentUser.getId());
                    roomList.put(roomId, integers);
                    tips = "创建";
                } else {
                    // 存在
                    Set<Integer> members = roomList.get(roomId);
                    members.add(this.currentUser.getId());
                }
                msg.setToId(this.currentUser.getId());
                msg.setUserId(0);
                msg.setContent(tips + "群聊成功");
                msg.setDate(new Date());
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(msg));
                System.out.println(roomList);
                break;
            case 2:
                // 退出群聊
                Set<Integer> integers = roomList.get(msg.getRoomId());
                msg.setContent("退出群聊失败");
                System.out.println(roomList);
                if (integers != null && !integers.isEmpty())  {
                    // 群存在，则移除元素
                    integers.remove(this.currentUser.getId());
                    roomList.put(msg.getRoomId(),integers);
                    msg.setContent("退出群聊成功");
                }
                msg.setToId(this.currentUser.getId());
                msg.setUserId(0);
                msg.setDate(new Date());
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(msg));
                break;
            default:
                // 非法操作
                msg.setToId(this.currentUser.getId());
                msg.setUserId(0);
                msg.setContent("非法操作");
                msg.setDate(new Date());
                session.getBasicRemote().sendText(objectMapper.writeValueAsString(msg));
                break;
        }
    }

    /**
     * 错误的时候调用
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable){
        System.out.println("发生错误");
        throwable.printStackTrace();
    }

    /**
     * 判断是否认证通过
     * @param token
     * @return
     */
    public Boolean authenticate(String token) {
        LambdaQueryWrapper<ChatUser> lq = new LambdaQueryWrapper<>();
        lq.eq(ChatUser::getLoginSign,token);
        lq.eq(ChatUser::getStatus,1);
        ChatUser user = userService.getOne(lq);
        if (user == null) {
            return false;
        }
        this.currentUser = user;
        return true;
    }

    /**
     * 添加在线人数
     */
    public static void addOnlineCount(){
        onlineNum.incrementAndGet();
    }

    /**
     * 减少在线人数
     */
    public static void subOnlineCount() {
        onlineNum.decrementAndGet();
    }

    /**
     * 获取指定房间成员ID
     * @param roomId
     * @return
     */
    public static Set<Integer> getRoomList(Integer roomId) {
        return roomList.getOrDefault(roomId, null);
    }

    /**
     * 获取在线人数
     * @return
     */
    public static int getOnlineNum() {
        return onlineNum.get();
    }
}
