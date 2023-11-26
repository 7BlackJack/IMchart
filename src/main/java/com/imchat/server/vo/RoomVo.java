package com.imchat.server.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/26
 */
@Data
public class RoomVo {
    /**
     *
     */
    private Integer id;

    /**
     *
     */
    private Integer userId;

    /**
     * 房间名
     */
    private String name;

    /**
     *
     */
    private Integer limitNum;

    /**
     * 房主账号
     */
    private String masterName;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

}
