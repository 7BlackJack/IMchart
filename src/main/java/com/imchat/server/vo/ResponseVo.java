package com.imchat.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author Efar <efarxs@163.com>
 * @Version V1.0.0
 * @Since 1.0
 * @Date 2023/11/26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseVo<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> ResponseVo<T> success(String msg,T data){
        return new ResponseVo<>(200, msg, data);
    }

    public static <T> ResponseVo<T> success(){
        return new ResponseVo<>(200, "操作成功", null);
    }

    public static <T> ResponseVo<T> success(String msg){
        return new ResponseVo<>(200, msg, null);
    }

    public static <T> ResponseVo<T> success(T data){
        return new ResponseVo<>(200, "操作成功", data);
    }

    public static <T> ResponseVo<T> error(int code, String message) {
        return new ResponseVo<>(code, message, null);
    }
}
