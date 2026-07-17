package com.IusCloud.messaging.shared.enums;

/**
 * Desde qué línea de WhatsApp sale una notificación.
 *
 * <p>Son dos líneas distintas y no intercambiables:
 * <ul>
 *   <li>{@link #TENANT} — la instancia que el abogado conectó escaneando su QR. Es la
 *       que usa para hablarle a SUS clientes. Si no la ha conectado, el envío falla.</li>
 *   <li>{@link #PLATFORM} — la línea propia de IusCloud
 *       ({@code messaging.platform.instance-name}). Se usa cuando quien habla es
 *       IusCloud y no el abogado: el OTP de registro, o los avisos del producto al
 *       propio abogado. No depende de que el tenant haya conectado nada.</li>
 * </ul>
 */
public enum NotificationSender {
    TENANT,
    PLATFORM
}
