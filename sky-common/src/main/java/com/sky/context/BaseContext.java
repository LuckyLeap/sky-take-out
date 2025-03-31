package com.sky.context;

/**
 * 基于ThreadLocal封装工具类
 */
public class BaseContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isSet = new ThreadLocal<>(); // 标记是否被设置过

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
        isSet.set(true); // 标记为已设置
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
        isSet.remove();
    }

    public static boolean isIdSet() {
        return Boolean.TRUE.equals(isSet.get());
    }
}