# ms-mensajeria — Guía de integración para el Frontend

**Base URL (dev):** `http://localhost:8084/ms-messaging`  
**Base URL (prod):** `https://<dominio>/ms-messaging`  
**Autenticación:** Bearer JWT en todas las rutas excepto `POST /api/v1/webhooks/evolution`

---

## Flujo completo de vinculación de WhatsApp

```
1. PUT  /api/v1/instances          → registrar nombre de instancia Evolution
2. POST /api/v1/instances/connect  → obtener QR (base64) y mostrar al usuario
3. Usuario escanea el QR con su WhatsApp
4. Evolution llama al webhook → el estado cambia a CONNECTED automáticamente
5. GET  /api/v1/instances          → verificar que status === "CONNECTED"
```

---

## 1. Gestión de instancia WhatsApp

### 1.1 Registrar / actualizar instancia

Debe llamarse antes de conectar. Vincula el tenant con un nombre de instancia en Evolution API.

```
PUT /api/v1/instances
Authorization: Bearer <token>
Content-Type: application/json
```

**Request body:**
```json
{
  "instanceName": "iuscloud-tenant-abc",
  "phoneNumber": "573001234567",
  "webhookSecret": "mi-secret-seguro"
}
```

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `instanceName` | string | Sí | Nombre único de la instancia en Evolution API |
| `phoneNumber` | string | No | Número de teléfono asociado |
| `webhookSecret` | string | No | Secret para validar webhooks (recomendado) |

**Response 200:**
```json
{
  "status": 200,
  "message": "Operación exitosa",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "tenantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "instanceName": "iuscloud-tenant-abc",
    "phoneNumber": "573001234567",
    "status": "DISCONNECTED",
    "lastConnectedAt": null,
    "createdAt": "2025-06-10T18:00:00Z",
    "updatedAt": "2025-06-10T18:00:00Z"
  }
}
```

---

### 1.2 Iniciar conexión y obtener QR

Llama a Evolution API para generar el código QR. El estado pasa a `QR_PENDING`.  
**El QR expira en ~60 segundos.** Si el usuario no escanea a tiempo, vuelve a llamar este endpoint.

```
POST /api/v1/instances/connect
Authorization: Bearer <token>
```

_(Sin body)_

**Response 200:**
```json
{
  "status": 200,
  "message": "Operación exitosa",
  "data": {
    "instanceName": "iuscloud-tenant-abc",
    "status": "QR_PENDING",
    "base64Qr": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "pairingCode": "ABCD-EFGH-IJKL-MNOP"
  }
}
```

| Campo | Descripción |
|-------|-------------|
| `base64Qr` | Imagen del QR en base64. Renderizar con `<img src="{{base64Qr}}" />` |
| `pairingCode` | Código alternativo para vincular sin QR (WhatsApp > Dispositivos vinculados > Vincular con número) |

**Errores posibles:**

| Código HTTP | Error | Causa |
|-------------|-------|-------|
| 404 | `ResourceNotFoundException` | No se ha registrado instancia (llama PUT primero) |
| 409 | `ALREADY_CONNECTED` | La instancia ya está conectada |
| 502 | `BusinessException` | Evolution API no disponible |

---

### 1.3 Consultar estado de la instancia

Usar para polling ligero después de mostrar el QR (sugerido: cada 3-5 segundos).  
Cuando `status === "CONNECTED"`, el WhatsApp está vinculado y listo para enviar.

```
GET /api/v1/instances
Authorization: Bearer <token>
```

**Response 200:**
```json
{
  "status": 200,
  "message": "Operación exitosa",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "tenantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "instanceName": "iuscloud-tenant-abc",
    "phoneNumber": "573001234567",
    "status": "CONNECTED",
    "lastConnectedAt": "2025-06-10T18:05:23Z",
    "createdAt": "2025-06-10T18:00:00Z",
    "updatedAt": "2025-06-10T18:05:23Z"
  }
}
```

**Posibles valores de `status`:**

| Valor | Significado | Acción UI |
|-------|-------------|-----------|
| `DISCONNECTED` | Sin conexión | Mostrar botón "Vincular WhatsApp" |
| `CONNECTING` | Conectando (transitorio) | Mostrar spinner |
| `QR_PENDING` | Esperando escaneo | Mostrar QR |
| `CONNECTED` | Vinculado y listo | Mostrar estado activo |
| `FAILED` | Error de conexión | Mostrar error + botón reintentar |

---

## 2. Envío de notificaciones / mensajes

### 2.1 Enviar mensaje WhatsApp

```
POST /api/v1/notifications
Authorization: Bearer <token>
Content-Type: application/json
```

**Request body:**
```json
{
  "templateCode": "GENERIC",
  "recipientPhone": "573001234567",
  "clientId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "variables": {
    "nombre": "Juan Pérez",
    "fecha": "15 de junio de 2025",
    "hora": "10:00 AM"
  },
  "scheduledAt": null,
  "idempotencyKey": "notif-caso-123-audiencia-456",
  "payload": {}
}
```

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `templateCode` | string | Sí | Código del template (ver tabla abajo) |
| `recipientPhone` | string | Sí | Número destino con código de país (ej: `573001234567`) |
| `clientId` | UUID | No | ID del cliente/caso asociado |
| `variables` | object | No | Variables para renderizar el template |
| `scheduledAt` | ISO 8601 | No | Fecha de envío programado. `null` = inmediato |
| `idempotencyKey` | string | No | Clave para evitar duplicados en reintentos |

**Templates disponibles:**

| `templateCode` | Descripción | Variables esperadas |
|----------------|-------------|---------------------|
| `HEARING_REMINDER` | Recordatorio de audiencia | `nombre`, `fecha`, `hora`, `juzgado` |
| `HEARING_SCHEDULED` | Audiencia programada | `nombre`, `fecha`, `hora`, `tipo` |
| `TASK_ASSIGNED` | Actividad asignada | `nombre`, `tarea`, `vencimiento` |
| `CASE_STATUS_CHANGED` | Cambio de estado del caso | `nombre`, `estado`, `caso` |
| `DOCUMENT_REQUESTED` | Solicitud de documento | `nombre`, `documento`, `plazo` |
| `GENERIC` | Mensaje libre | Cualquier clave usada en el texto del template |

**Response 200:**
```json
{
  "status": 200,
  "message": "Operación exitosa",
  "data": {
    "id": "7f3b9c1a-2e4d-5f6a-8b9c-0d1e2f3a4b5c",
    "tenantId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "recipientPhone": "573001234567",
    "clientId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "templateCode": "GENERIC",
    "renderedContent": "Hola Juan Pérez, su audiencia está programada para el 15 de junio de 2025 a las 10:00 AM.",
    "status": "SENT",
    "evolutionMessageId": "3EB0F1A2B3C4D5E6F7",
    "scheduledAt": null,
    "sentAt": "2025-06-10T18:10:00Z",
    "deliveredAt": null,
    "readAt": null,
    "error": null,
    "retryCount": 0,
    "createdAt": "2025-06-10T18:10:00Z",
    "updatedAt": "2025-06-10T18:10:00Z"
  }
}
```

---

### 2.2 Consultar estado de un mensaje

```
GET /api/v1/notifications/{id}
Authorization: Bearer <token>
```

**Response 200:** Misma estructura que el POST anterior.

**Posibles valores de `status`:**

| Valor | Descripción |
|-------|-------------|
| `PENDING` | En cola, aún no enviado |
| `SCHEDULED` | Programado para fecha futura |
| `SENT` | Enviado a Evolution API |
| `DELIVERED` | Entregado al teléfono del destinatario |
| `READ` | Leído por el destinatario |
| `FAILED` | Error en el envío |

---

## 3. Webhook de Evolution API (solo backend)

> Este endpoint es llamado **automáticamente por Evolution API**, no por el frontend.  
> Documentado aquí para referencia de configuración.

```
POST /api/v1/webhooks/evolution
Content-Type: application/json
(Sin autenticación JWT)
```

Evolution debe configurarse con:
- **Webhook URL:** `https://<dominio>/ms-messaging/api/v1/webhooks/evolution`
- **Eventos:** `messages.upsert`, `messages.update`, `connection.update`

---

## 4. Diagrama de flujo de vinculación

```
Frontend                    ms-mensajeria              Evolution API
   |                             |                           |
   |-- PUT /instances ---------->|                           |
   |<-- { status: DISCONNECTED } |                           |
   |                             |                           |
   |-- POST /instances/connect ->|                           |
   |                             |-- GET /instance/connect ->|
   |                             |<-- { base64Qr, code } ----|
   |<-- { base64Qr, status: QR_PENDING }                     |
   |                             |                           |
   | [Muestra QR al usuario]     |                           |
   |                             |                           |
   | [Usuario escanea QR]        |                           |
   |                             |<-- webhook connection.update { state: "open" }
   |                             | [status → CONNECTED]      |
   |                             |                           |
   |-- GET /instances (polling)->|                           |
   |<-- { status: CONNECTED } ---|                           |
   |                             |                           |
   | [Muestra "WhatsApp activo"] |                           |
```

---

## 5. Manejo de errores

Todos los errores siguen esta estructura:

```json
{
  "timestamp": "2025-06-10T18:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción del error",
  "code": "BUSINESS_ERROR"
}
```

| HTTP | Causa típica |
|------|-------------|
| 400 | Validación fallida (campo requerido faltante) |
| 401 | Token JWT inválido o expirado |
| 404 | Recurso no encontrado |
| 409 | `ALREADY_CONNECTED` — instancia ya vinculada |
| 500 | Error interno |
| 502 | Evolution API no disponible |

---

## 6. Colección Postman

Copiar el JSON completo a un archivo `.json` e importar en Postman con **File → Import**.

```json
{
  "info": {
    "name": "ms-mensajeria — IusCloud",
    "description": "Colección completa del microservicio de mensajería WhatsApp vía Evolution API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8084/ms-messaging",
      "type": "string"
    },
    {
      "key": "token",
      "value": "PEGAR_TOKEN_JWT_AQUI",
      "type": "string"
    },
    {
      "key": "notification_id",
      "value": "",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "Instancias WhatsApp",
      "item": [
        {
          "name": "1. Registrar instancia",
          "request": {
            "method": "PUT",
            "header": [
              { "key": "Authorization", "value": "Bearer {{token}}" },
              { "key": "Content-Type", "value": "application/json" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/instances",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "instances"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"instanceName\": \"iuscloud-tenant-demo\",\n  \"phoneNumber\": \"573001234567\",\n  \"webhookSecret\": \"mi-secret-seguro-123\"\n}",
              "options": { "raw": { "language": "json" } }
            },
            "description": "Registra o actualiza la instancia Evolution del tenant. Debe llamarse antes de conectar."
          },
          "response": [
            {
              "name": "200 - Instancia registrada",
              "status": "OK",
              "code": 200,
              "body": "{\n  \"status\": 200,\n  \"message\": \"Operación exitosa\",\n  \"data\": {\n    \"id\": \"550e8400-e29b-41d4-a716-446655440000\",\n    \"tenantId\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\",\n    \"instanceName\": \"iuscloud-tenant-demo\",\n    \"phoneNumber\": \"573001234567\",\n    \"status\": \"DISCONNECTED\",\n    \"lastConnectedAt\": null,\n    \"createdAt\": \"2025-06-10T18:00:00Z\",\n    \"updatedAt\": \"2025-06-10T18:00:00Z\"\n  }\n}"
            }
          ]
        },
        {
          "name": "2. Iniciar conexión (obtener QR)",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "const res = pm.response.json();",
                  "if (res.data && res.data.base64Qr) {",
                  "    pm.collectionVariables.set('qr_base64', res.data.base64Qr);",
                  "    console.log('QR guardado. Pega el valor de qr_base64 en un visor de imágenes base64.');",
                  "}"
                ],
                "type": "text/javascript"
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              { "key": "Authorization", "value": "Bearer {{token}}" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/instances/connect",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "instances", "connect"]
            },
            "description": "Llama a Evolution API para generar el QR. El QR expira en ~60s. Si el usuario no escanea a tiempo, vuelve a llamar este endpoint."
          },
          "response": [
            {
              "name": "200 - QR generado",
              "status": "OK",
              "code": 200,
              "body": "{\n  \"status\": 200,\n  \"message\": \"Operación exitosa\",\n  \"data\": {\n    \"instanceName\": \"iuscloud-tenant-demo\",\n    \"status\": \"QR_PENDING\",\n    \"base64Qr\": \"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...\",\n    \"pairingCode\": \"ABCD-EFGH-IJKL-MNOP\"\n  }\n}"
            },
            {
              "name": "404 - Sin instancia configurada",
              "status": "Not Found",
              "code": 404,
              "body": "{\n  \"timestamp\": \"2025-06-10T18:00:00Z\",\n  \"status\": 404,\n  \"error\": \"Not Found\",\n  \"message\": \"No hay instancia configurada. Usa PUT /api/v1/instances primero.\",\n  \"code\": \"RESOURCE_NOT_FOUND\"\n}"
            },
            {
              "name": "409 - Ya conectada",
              "status": "Conflict",
              "code": 409,
              "body": "{\n  \"timestamp\": \"2025-06-10T18:00:00Z\",\n  \"status\": 409,\n  \"error\": \"Conflict\",\n  \"message\": \"La instancia ya está conectada. Desconéctala primero si deseas re-vincular.\",\n  \"code\": \"ALREADY_CONNECTED\"\n}"
            }
          ]
        },
        {
          "name": "3. Consultar estado de la instancia (polling)",
          "request": {
            "method": "GET",
            "header": [
              { "key": "Authorization", "value": "Bearer {{token}}" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/instances",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "instances"]
            },
            "description": "Consultar estado actual. Hacer polling cada 3-5 segundos después de mostrar el QR. Detener cuando status === CONNECTED."
          },
          "response": [
            {
              "name": "200 - Conectado",
              "status": "OK",
              "code": 200,
              "body": "{\n  \"status\": 200,\n  \"message\": \"Operación exitosa\",\n  \"data\": {\n    \"id\": \"550e8400-e29b-41d4-a716-446655440000\",\n    \"tenantId\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\",\n    \"instanceName\": \"iuscloud-tenant-demo\",\n    \"phoneNumber\": \"573001234567\",\n    \"status\": \"CONNECTED\",\n    \"lastConnectedAt\": \"2025-06-10T18:05:23Z\",\n    \"createdAt\": \"2025-06-10T18:00:00Z\",\n    \"updatedAt\": \"2025-06-10T18:05:23Z\"\n  }\n}"
            },
            {
              "name": "200 - QR pendiente",
              "status": "OK",
              "code": 200,
              "body": "{\n  \"status\": 200,\n  \"message\": \"Operación exitosa\",\n  \"data\": {\n    \"id\": \"550e8400-e29b-41d4-a716-446655440000\",\n    \"tenantId\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\",\n    \"instanceName\": \"iuscloud-tenant-demo\",\n    \"phoneNumber\": \"573001234567\",\n    \"status\": \"QR_PENDING\",\n    \"lastConnectedAt\": null,\n    \"createdAt\": \"2025-06-10T18:00:00Z\",\n    \"updatedAt\": \"2025-06-10T18:03:00Z\"\n  }\n}"
            }
          ]
        }
      ]
    },
    {
      "name": "Notificaciones / Mensajes",
      "item": [
        {
          "name": "Enviar mensaje - Template GENERIC",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "const res = pm.response.json();",
                  "if (res.data && res.data.id) {",
                  "    pm.collectionVariables.set('notification_id', res.data.id);",
                  "}"
                ],
                "type": "text/javascript"
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              { "key": "Authorization", "value": "Bearer {{token}}" },
              { "key": "Content-Type", "value": "application/json" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/notifications",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "notifications"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"templateCode\": \"GENERIC\",\n  \"recipientPhone\": \"573001234567\",\n  \"clientId\": null,\n  \"variables\": {\n    \"nombre\": \"Juan Pérez\",\n    \"mensaje\": \"Su caso ha sido actualizado.\"\n  },\n  \"scheduledAt\": null,\n  \"idempotencyKey\": \"test-msg-001\",\n  \"payload\": {}\n}",
              "options": { "raw": { "language": "json" } }
            }
          }
        },
        {
          "name": "Enviar mensaje - Recordatorio de audiencia",
          "request": {
            "method": "POST",
            "header": [
              { "key": "Authorization", "value": "Bearer {{token}}" },
              { "key": "Content-Type", "value": "application/json" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/notifications",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "notifications"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"templateCode\": \"HEARING_REMINDER\",\n  \"recipientPhone\": \"573001234567\",\n  \"clientId\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\",\n  \"variables\": {\n    \"nombre\": \"Juan Pérez\",\n    \"fecha\": \"15 de junio de 2025\",\n    \"hora\": \"10:00 AM\",\n    \"juzgado\": \"Juzgado 5 Civil del Circuito\"\n  },\n  \"scheduledAt\": null,\n  \"idempotencyKey\": \"hearing-reminder-caso-123\",\n  \"payload\": {}\n}",
              "options": { "raw": { "language": "json" } }
            }
          }
        },
        {
          "name": "Enviar mensaje programado",
          "request": {
            "method": "POST",
            "header": [
              { "key": "Authorization", "value": "Bearer {{token}}" },
              { "key": "Content-Type", "value": "application/json" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/notifications",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "notifications"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"templateCode\": \"TASK_ASSIGNED\",\n  \"recipientPhone\": \"573001234567\",\n  \"clientId\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\",\n  \"variables\": {\n    \"nombre\": \"María López\",\n    \"tarea\": \"Entregar poderes notariados\",\n    \"vencimiento\": \"20 de junio de 2025\"\n  },\n  \"scheduledAt\": \"2025-06-14T09:00:00Z\",\n  \"idempotencyKey\": \"task-maria-poderes-20250614\",\n  \"payload\": {}\n}",
              "options": { "raw": { "language": "json" } }
            }
          }
        },
        {
          "name": "Consultar estado de mensaje",
          "request": {
            "method": "GET",
            "header": [
              { "key": "Authorization", "value": "Bearer {{token}}" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/notifications/{{notification_id}}",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "notifications", "{{notification_id}}"]
            },
            "description": "El ID se guarda automáticamente en la variable notification_id al ejecutar el POST anterior."
          },
          "response": [
            {
              "name": "200 - Mensaje entregado",
              "status": "OK",
              "code": 200,
              "body": "{\n  \"status\": 200,\n  \"message\": \"Operación exitosa\",\n  \"data\": {\n    \"id\": \"7f3b9c1a-2e4d-5f6a-8b9c-0d1e2f3a4b5c\",\n    \"tenantId\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\",\n    \"recipientPhone\": \"573001234567\",\n    \"clientId\": \"a1b2c3d4-e5f6-7890-abcd-ef1234567890\",\n    \"templateCode\": \"GENERIC\",\n    \"renderedContent\": \"Hola Juan Pérez, su caso ha sido actualizado.\",\n    \"status\": \"DELIVERED\",\n    \"evolutionMessageId\": \"3EB0F1A2B3C4D5E6F7\",\n    \"scheduledAt\": null,\n    \"sentAt\": \"2025-06-10T18:10:00Z\",\n    \"deliveredAt\": \"2025-06-10T18:10:05Z\",\n    \"readAt\": null,\n    \"error\": null,\n    \"retryCount\": 0,\n    \"createdAt\": \"2025-06-10T18:10:00Z\",\n    \"updatedAt\": \"2025-06-10T18:10:05Z\"\n  }\n}"
            }
          ]
        }
      ]
    },
    {
      "name": "Webhook Evolution (referencia)",
      "item": [
        {
          "name": "Simular connection.update CONNECTED",
          "request": {
            "method": "POST",
            "header": [
              { "key": "Content-Type", "value": "application/json" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/webhooks/evolution",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "webhooks", "evolution"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"event\": \"connection.update\",\n  \"instance\": \"iuscloud-tenant-demo\",\n  \"data\": {\n    \"instance\": \"iuscloud-tenant-demo\",\n    \"state\": \"open\",\n    \"statusReason\": 200\n  }\n}",
              "options": { "raw": { "language": "json" } }
            },
            "description": "Solo para pruebas locales. Evolution llama esto automáticamente cuando el usuario escanea el QR. Cambia el status de la instancia a CONNECTED."
          }
        },
        {
          "name": "Simular connection.update DISCONNECTED",
          "request": {
            "method": "POST",
            "header": [
              { "key": "Content-Type", "value": "application/json" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/webhooks/evolution",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "webhooks", "evolution"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"event\": \"connection.update\",\n  \"instance\": \"iuscloud-tenant-demo\",\n  \"data\": {\n    \"instance\": \"iuscloud-tenant-demo\",\n    \"state\": \"close\",\n    \"statusReason\": 428\n  }\n}",
              "options": { "raw": { "language": "json" } }
            }
          }
        },
        {
          "name": "Simular mensaje entrante",
          "request": {
            "method": "POST",
            "header": [
              { "key": "Content-Type", "value": "application/json" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/webhooks/evolution",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "webhooks", "evolution"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"event\": \"messages.upsert\",\n  \"instance\": \"iuscloud-tenant-demo\",\n  \"data\": {\n    \"key\": {\n      \"remoteJid\": \"573001234567@s.whatsapp.net\",\n      \"fromMe\": false,\n      \"id\": \"MSG_ID_ENTRANTE_001\"\n    },\n    \"message\": {\n      \"conversation\": \"Hola, tengo una pregunta sobre mi caso.\"\n    },\n    \"messageType\": \"conversation\",\n    \"pushName\": \"Juan Pérez\"\n  }\n}",
              "options": { "raw": { "language": "json" } }
            }
          }
        },
        {
          "name": "Simular delivery ACK (mensaje entregado)",
          "request": {
            "method": "POST",
            "header": [
              { "key": "Content-Type", "value": "application/json" }
            ],
            "url": {
              "raw": "{{base_url}}/api/v1/webhooks/evolution",
              "host": ["{{base_url}}"],
              "path": ["api", "v1", "webhooks", "evolution"]
            },
            "body": {
              "mode": "raw",
              "raw": "{\n  \"event\": \"messages.update\",\n  \"instance\": \"iuscloud-tenant-demo\",\n  \"data\": {\n    \"key\": {\n      \"remoteJid\": \"573001234567@s.whatsapp.net\",\n      \"fromMe\": true,\n      \"id\": \"3EB0F1A2B3C4D5E6F7\"\n    },\n    \"status\": \"DELIVERED\"\n  }\n}",
              "options": { "raw": { "language": "json" } }
            }
          }
        }
      ]
    }
  ]
}
```

---

## 7. Configuración de Evolution API (para el equipo de backend/infra)

En Evolution API, configurar el webhook global o por instancia apuntando a:

```
POST https://<dominio>/ms-messaging/api/v1/webhooks/evolution
```

Eventos a habilitar:
- `MESSAGES_UPSERT`
- `MESSAGES_UPDATE`
- `CONNECTION_UPDATE`

---

## Notas de implementación para el frontend

1. **No almacenar el `base64Qr` en estado global** — expira en ~60 segundos. Pedirlo fresco cada vez que el usuario abra el modal de vinculación.

2. **Polling recomendado:** `GET /instances` cada 4 segundos mientras `status !== "CONNECTED"`. Detener al conectar o tras 90 segundos (timeout de QR + margen).

3. **`pairingCode`** es una alternativa al QR para dispositivos donde la cámara no está disponible. Mostrarlo debajo del QR con instrucciones: _"WhatsApp → Dispositivos vinculados → Vincular con número de teléfono"_.

4. **Números de teléfono:** enviar siempre con código de país sin `+` ni espacios. Ejemplo: `573001234567` para Colombia.

5. **Idempotency key:** usar un ID semánticamente único por operación (ej: `reminder-{caseId}-{hearingId}-{date}`) para que los reintentos del frontend no dupliquen mensajes.
