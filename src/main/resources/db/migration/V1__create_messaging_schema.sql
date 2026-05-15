-- ============================================================
-- whatsapp_instances: conexión Evolution API por tenant
-- ============================================================
CREATE TABLE whatsapp_instances (
    id                  UUID         PRIMARY KEY,
    tenant_id           UUID         NOT NULL,
    instance_name       VARCHAR(120) NOT NULL,
    phone_number        VARCHAR(30),
    status              VARCHAR(20)  NOT NULL DEFAULT 'DISCONNECTED',
    webhook_secret      VARCHAR(120),
    last_connected_at   TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    deleted_at          TIMESTAMP,
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX uq_whatsapp_instances_tenant
    ON whatsapp_instances(tenant_id)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uq_whatsapp_instances_name
    ON whatsapp_instances(instance_name)
    WHERE deleted_at IS NULL;

-- ============================================================
-- notifications: envíos outbound (inmediatos y programados)
-- ============================================================
CREATE TABLE notifications (
    id                      UUID         PRIMARY KEY,
    tenant_id               UUID         NOT NULL,
    recipient_phone         VARCHAR(30)  NOT NULL,
    client_id               UUID,
    template_code           VARCHAR(60)  NOT NULL,
    rendered_content        TEXT         NOT NULL,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    evolution_message_id    VARCHAR(120),
    scheduled_at            TIMESTAMP,
    sent_at                 TIMESTAMP,
    delivered_at            TIMESTAMP,
    read_at                 TIMESTAMP,
    error                   TEXT,
    retry_count             INT          NOT NULL DEFAULT 0,
    payload                 JSONB,
    idempotency_key         VARCHAR(120),
    created_at              TIMESTAMP    NOT NULL,
    updated_at              TIMESTAMP    NOT NULL,
    deleted_at              TIMESTAMP,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX ix_notifications_status_scheduled
    ON notifications(tenant_id, status, scheduled_at);

CREATE INDEX ix_notifications_tenant_created
    ON notifications(tenant_id, created_at DESC);

CREATE UNIQUE INDEX uq_notifications_evolution_id
    ON notifications(evolution_message_id)
    WHERE evolution_message_id IS NOT NULL;

CREATE UNIQUE INDEX uq_notifications_idempotency
    ON notifications(tenant_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

-- ============================================================
-- whatsapp_inbound_messages: respuestas recibidas (log)
-- ============================================================
CREATE TABLE whatsapp_inbound_messages (
    id                      UUID         PRIMARY KEY,
    tenant_id               UUID         NOT NULL,
    instance_id             UUID         NOT NULL REFERENCES whatsapp_instances(id),
    sender_phone            VARCHAR(30)  NOT NULL,
    client_id               UUID,
    content                 TEXT,
    media_url               VARCHAR(500),
    media_type              VARCHAR(30),
    received_at             TIMESTAMP    NOT NULL,
    evolution_message_id    VARCHAR(120),
    raw_payload             JSONB,
    created_at              TIMESTAMP    NOT NULL,
    updated_at              TIMESTAMP    NOT NULL,
    deleted_at              TIMESTAMP,
    is_active               BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX ix_inbound_tenant_received
    ON whatsapp_inbound_messages(tenant_id, received_at DESC);

CREATE UNIQUE INDEX uq_inbound_evolution_id
    ON whatsapp_inbound_messages(evolution_message_id)
    WHERE evolution_message_id IS NOT NULL;

-- ============================================================
-- webhook_events: log crudo de callbacks de Evolution
-- ============================================================
CREATE TABLE webhook_events (
    id              UUID         PRIMARY KEY,
    instance_id     UUID         REFERENCES whatsapp_instances(id),
    event_type      VARCHAR(60)  NOT NULL,
    payload         JSONB        NOT NULL,
    processed_at    TIMESTAMP,
    error           TEXT,
    received_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL,
    deleted_at      TIMESTAMP,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE INDEX ix_webhook_events_type_received
    ON webhook_events(event_type, received_at DESC);

CREATE INDEX ix_webhook_events_unprocessed
    ON webhook_events(processed_at)
    WHERE processed_at IS NULL;
