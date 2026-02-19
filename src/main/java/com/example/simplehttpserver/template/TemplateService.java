package com.example.simplehttpserver.template;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

/**
 * Loads template files from classpath and renders them with {@link TemplateEngine}.
 */
public class TemplateService {

    private final String templatesRoot;
    private final TemplateEngine templateEngine = new TemplateEngine();

    public TemplateService(String templatesRoot) {
        this.templatesRoot = templatesRoot;
    }

    public String render(String templateName, Map<String, String> model) throws IOException {
        String sanitizedTemplateName = sanitizeTemplateName(templateName);
        String resourceName = templatesRoot + "/" + sanitizedTemplateName;

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IOException("Template not found: " + resourceName);
            }

            String template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return templateEngine.render(template, model);
        }
    }

    private String sanitizeTemplateName(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            throw new IllegalArgumentException("Template name must not be blank.");
        }

        Path normalized = Path.of(templateName).normalize();
        if (normalized.isAbsolute() || normalized.startsWith("..")) {
            throw new IllegalArgumentException("Invalid template name: " + templateName);
        }

        return normalized.toString().replace('\\', '/');
    }
}
