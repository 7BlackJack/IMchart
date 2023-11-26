package com.imchat.server.websocket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.imchat.server.entity.ChatUser;
import com.imchat.server.service.ChatUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Arrays;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/26
 */
@Component
@Slf4j
public class WebsocketEndpointConfig extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // 判断是否登录 只有登录了才能连接，那么就根据自定义的token请求头来判断
        String token = request.getHeaders().getOrDefault("token", Arrays.asList("")).get(0);
        sec.getUserProperties().put("token", token);
        super.modifyHandshake(sec, request, response);
    }
}
