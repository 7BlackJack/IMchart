package com.imchat.server;

import com.imchat.server.entity.ChatUser;
import com.imchat.server.service.ChatUserService;
import lombok.With;
import org.apache.tomcat.util.security.MD5Encoder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
class ImChatHomeworkApplicationTests {

    @Autowired
    private ChatUserService chatUserService;

    @Test
    void contextLoads() {

    }

}
