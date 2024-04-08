package com.heima.utils.thread;

import com.heima.model.wemedia.pojos.WmUser;

public class WmThreadLocalUtil {
    private static final ThreadLocal<WmUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 添加用户
     */
    public static void setWmUser(WmUser user) {
        WM_USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取用户
     */
    public static WmUser getWmUser() {
        return WM_USER_THREAD_LOCAL.get();
    }

    /**
     * 移除用户
     */
    public static void clear() {
        WM_USER_THREAD_LOCAL.remove();
    }
}
