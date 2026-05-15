package com.IusCloud.messaging.shared.tenant;

import java.util.UUID;

public final class UserContext {

    private static final ThreadLocal<UUID> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> RESTRICTED = new ThreadLocal<>();

    private UserContext() {}

    public static void setUserId(UUID userId) {
        CURRENT_USER.set(userId);
    }

    public static UUID getUserId() {
        return CURRENT_USER.get();
    }

    public static boolean isAuthenticated() {
        return CURRENT_USER.get() != null;
    }

    /**
     * Marca si el usuario está restringido a ver únicamente los recursos
     * en los que participa (ej. casos donde es miembro). Los administradores
     * no están restringidos y ven todo el tenant.
     */
    public static void setRestricted(boolean restricted) {
        RESTRICTED.set(restricted);
    }

    public static boolean isRestricted() {
        return Boolean.TRUE.equals(RESTRICTED.get());
    }

    public static void clear() {
        CURRENT_USER.remove();
        RESTRICTED.remove();
    }
}