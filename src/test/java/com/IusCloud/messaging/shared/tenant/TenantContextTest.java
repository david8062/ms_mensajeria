package com.IusCloud.messaging.shared.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldSetAndGetTenantId() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        Assertions.assertEquals(tenantId, TenantContext.getTenantId());
        Assertions.assertTrue(TenantContext.hasTenant());
    }

    @Test
    void shouldThrowExceptionWhenTenantNotSet() {
        Assertions.assertThrows(IllegalStateException.class, TenantContext::getTenantId);
        Assertions.assertFalse(TenantContext.hasTenant());
    }

    @Test
    void shouldClearContext() {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
        TenantContext.clear();

        Assertions.assertFalse(TenantContext.hasTenant());
        Assertions.assertThrows(IllegalStateException.class, TenantContext::getTenantId);
    }
}