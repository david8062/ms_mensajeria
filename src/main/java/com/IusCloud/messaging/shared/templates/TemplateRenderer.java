package com.IusCloud.messaging.shared.templates;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

    public String render(NotificationTemplate template, Map<String, Object> variables) {
        String raw = template.getTemplate();
        Matcher matcher = PLACEHOLDER.matcher(raw);
        StringBuilder out = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = variables != null ? variables.get(key) : null;
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(out);

        return out.toString();
    }
}
