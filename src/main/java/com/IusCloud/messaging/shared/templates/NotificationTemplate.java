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
    );

    private final String template;

    NotificationTemplate(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }
}
