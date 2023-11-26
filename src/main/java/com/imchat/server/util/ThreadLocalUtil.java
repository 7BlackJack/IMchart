package com.imchat.server.util;

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
public class ThreadLocalUtil {
    private static ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    public static void add(Object data) {
        threadLocal.set(data);
    }

    public static Object get() {
        return threadLocal.get();
    }
}
