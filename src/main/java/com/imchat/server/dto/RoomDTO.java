package com.imchat.server.dto;

import lombok.Data;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/26
 */
@Data
public class RoomDTO {
    private String name;
    private String password;
    private Integer limitNum;
}
