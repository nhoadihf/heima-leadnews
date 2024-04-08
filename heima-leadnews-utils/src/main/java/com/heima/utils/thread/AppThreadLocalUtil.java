package com.heima.utils.thread;

import com.heima.model.user.pojos.ApUser;

public class AppThreadLocalUtil {
    private static final ThreadLocal<ApUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 添加用户
     */
    public static void setWmUser(ApUser user) {
        WM_USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取用户
     */
    public static ApUser getWmUser() {
        return WM_USER_THREAD_LOCAL.get();
    }

    /**
     * 移除用户
     */
    public static void clear() {
        WM_USER_THREAD_LOCAL.remove();
    }
}
