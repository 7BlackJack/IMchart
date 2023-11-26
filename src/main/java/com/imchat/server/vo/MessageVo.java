package com.imchat.server.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/26
 */
@Data
public class MessageVo {
    private Integer id;

    /**
     *
     */
    private Integer roomId;

    /**
     *
     */
    private Integer userId;

    /**
     * 发送者用户名
     */
    private String userName;

    /**
     *
     */
    private String content;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     *
     */
    private Integer toId;


    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
