package com.qa.email.support;

import com.qa.email.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * The one place that knows how to make the application under test send an email.
 *
 * Default implementation POSTs {"email":"<address>"} to app.trigger.url.
 * Replace the body of {@link #send(String)} with a UI step (Selenium page object),
 * a RestAssured call, or a DB/queue insert - nothing else in the framework changes.
 */
public final class AppEmailTrigger {

    private static final Logger LOG = LogManager.getLogger(AppEmailTrigger.class);
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private AppEmailTrigger() {
    }

    public static boolean isConfigured() {
        return ConfigManager.optional("app.trigger.url").isPresent();
    }

    public static void send(String recipient) throws IOException, InterruptedException {
        String url = ConfigManager.get("app.trigger.url");
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"" + recipient + "\"}"))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        LOG.info("Trigger POST {} -> HTTP {}", url, response.statusCode());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Email trigger failed with HTTP " + response.statusCode()
                    + ": " + response.body());
        }
    }
}
