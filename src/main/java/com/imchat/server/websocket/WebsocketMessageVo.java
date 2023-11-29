package com.imchat.server.websocket;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/25
 */
@Data
public class WebsocketMessageVo {

    /**
     * 消息内容
     */
    private String content;

    /**
     * 群聊ID 可空
     */
    private Integer roomId;

    /**
     * 发送者ID
     */
    private Integer userId;

    /**
     * 发送者名字
     */
    private String userName;

    /**
     * 接收者ID
     */
    private Integer toId;

    /**
     * 操作
     * 0 -> 发送消息
     * 1 -> 加入群聊 根据roomId
     * 2 -> 退出群聊 根据roomId
     */
    private Integer action;

    /**
     * 消息类型 默认文本
     * 0 -> 文本消息
     * 1 -> 图片消息
     * 2 -> 语音消息
     */
    private Integer type;

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;
}
