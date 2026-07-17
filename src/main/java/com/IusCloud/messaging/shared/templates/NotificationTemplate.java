package com.IusCloud.messaging.shared.templates;

public enum NotificationTemplate {

    HEARING_REMINDER(
            "Hola {recipientName}, esperamos que estés bien.\n\n"
                    + "Te recordamos que tienes una audiencia programada para tu caso \"{caseTitle}\":\n\n"
                    + "- Fecha: {date}\n"
                    + "- Hora: {time}\n\n"
                    + "Si necesitas algo, no dudes en escribirnos."
    ),
    HEARING_SCHEDULED(
            "Hola {recipientName}, esperamos que estés bien.\n\n"
                    + "Te informamos que se ha programado una nueva audiencia para tu caso \"{caseTitle}\":\n\n"
                    + "- Fecha: {date}\n"
                    + "- Hora: {time}\n\n"
                    + "Quedamos atentos a cualquier consulta."
    ),
    TASK_ASSIGNED(
            "Hola {recipientName}, esperamos que estés bien.\n\n"
                    + "Queremos avisarte que hay una nueva actividad pendiente en tu caso \"{caseTitle}\":\n\n"
                    + "- {activityTitle}\n\n"
                    + "Si tienes dudas, estamos para ayudarte."
    ),
    CASE_STATUS_CHANGED(
            "Hola {recipientName}, esperamos que estés bien.\n\n"
                    + "Te informamos que tu caso \"{caseTitle}\" tuvo un cambio de estado:\n\n"
                    + "- Anterior: {oldStatus}\n"
                    + "- Actual: {newStatus}\n\n"
                    + "Si necesitas más detalles, no dudes en escribirnos."
    ),
    DOCUMENT_REQUESTED(
            "Hola {recipientName}, esperamos que estés bien.\n\n"
                    + "Necesitamos que nos hagas llegar el siguiente documento para tu caso \"{caseTitle}\":\n\n"
                    + "- {documentName}\n\n"
                    + "Gracias por tu colaboración. Si tienes alguna duda, estamos atentos."
    ),
    GENERIC(
            "{mensaje}"
    ),
    /**
     * Novedad de la Rama Judicial en un proceso en seguimiento.
     *
     * <p>El cuerpo se compone en legal-core y llega ya armado en {@code mensaje}, en
     * vez de describirse aquí con variables sueltas, por dos razones: el texto varía
     * en singular/plural y lleva una lista de actuaciones de largo variable, que un
     * reemplazo plano no sabe expresar; y así el modo sombra (que envía con GENERIC)
     * y el modo real producen EXACTAMENTE el mismo mensaje — si divergieran, la
     * sombra dejaría de ser una vista previa fiel, que es su único propósito.
     *
     * <p>Existe separada de GENERIC, aunque renderice igual, para que las alertas de
     * seguimiento sean contables y auditables por {@code template_code}.
     */
    CASE_PROCESS_UPDATE(
            "{mensaje}"
    ),
    OTP_VERIFICATION(
            "Hola, tu código de verificación para restablecer tu contraseña en IusCloud es:\n\n"
                    + "*{otp}*\n\n"
                    + "Este código es válido por 10 minutos. No lo compartas con nadie."
    ),
    SIGNUP_VERIFICATION(
            "¡Bienvenido a IusCloud! Tu código para crear tu cuenta es:\n\n"
                    + "*{otp}*\n\n"
                    + "Este código es válido por 10 minutos. No lo compartas con nadie.\n\n"
                    + "Si no estabas creando una cuenta, ignora este mensaje."
    );

    private final String template;

    NotificationTemplate(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }
}
