package com.imchat.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@MapperScan(value = "com.imchat.server.**.mapper")
@EnableWebSocket
public class ImChatHomeworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImChatHomeworkApplication.class, args);
    }

}
