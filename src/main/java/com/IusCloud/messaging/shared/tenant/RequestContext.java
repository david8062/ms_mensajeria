package com.IusCloud.messaging.shared.tenant;

public final class RequestContext {

    private static final ThreadLocal<String> CURRENT_IP         = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_AGENT = new ThreadLocal<>();

    private RequestContext() {}

    public static void set(String ipAddress, String userAgent) {
        CURRENT_IP.set(ipAddress);
        CURRENT_USER_AGENT.set(userAgent);
    }

    public static String getIpAddress() {
        return CURRENT_IP.get();
    }

    public static String getUserAgent() {
        return CURRENT_USER_AGENT.get();
    }

    public static void clear() {
        CURRENT_IP.remove();
        CURRENT_USER_AGENT.remove();
    }
}
