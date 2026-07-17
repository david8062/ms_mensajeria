-- Desde qué línea sale cada notificación: la instancia del tenant (por defecto,
-- comportamiento histórico) o la línea propia de IusCloud.
--
-- PLATFORM es necesario para los avisos que manda IusCloud al abogado (p. ej. las
-- novedades de la Rama Judicial): no pueden depender de que el tenant haya
-- conectado su propio WhatsApp, porque si no lo hizo el envío termina en FAILED.
ALTER TABLE notifications
    ADD COLUMN sender VARCHAR(20) NOT NULL DEFAULT 'TENANT';
