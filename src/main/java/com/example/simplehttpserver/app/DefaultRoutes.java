package com.example.simplehttpserver.app;

import com.example.simplehttpserver.http.HttpMethod;
import com.example.simplehttpserver.http.HttpResponse;
import com.example.simplehttpserver.http.HttpStatus;
import com.example.simplehttpserver.routing.Router;
import com.example.simplehttpserver.template.TemplateService;
import com.example.simplehttpserver.util.JsonUtil;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registers built-in educational route examples.
 */
public final class DefaultRoutes {

    private DefaultRoutes() {
    }

    public static void register(Router router, TemplateService templateService) {
        router.addRoute(HttpMethod.GET, "/hello", context ->
                HttpResponse.text(HttpStatus.OK, "Hello World"));

        router.addRoute(HttpMethod.GET, "/echo", context -> {
            String json = "{"
                    + "\"path\":\"" + JsonUtil.escape(context.request().path()) + "\","
                    + "\"query\":" + JsonUtil.toQueryJson(context.queryParams())
                    + "}";
            return HttpResponse.json(HttpStatus.OK, json);
        });

        router.addRoute(HttpMethod.GET, "/template", context -> {
            int visits = Integer.parseInt(context.session().get("templateVisits").orElse("0")) + 1;
            context.session().put("templateVisits", Integer.toString(visits));

            String name = context.queryParam("name").orElse("Student");
            Map<String, String> model = new LinkedHashMap<>();
            model.put("name", name);
            model.put("visits", Integer.toString(visits));
            model.put("timestamp", LocalDateTime.now().toString());

            String html = templateService.render("demo.html.tpl", model);
            return HttpResponse.html(HttpStatus.OK, html);
        });

        router.addRoute(HttpMethod.GET, "/users/{id}", context -> {
            String id = context.pathParam("id").orElse("unknown");
            return HttpResponse.json(HttpStatus.OK, "{\"userId\":\"" + JsonUtil.escape(id) + "\"}");
        });
    }
}
