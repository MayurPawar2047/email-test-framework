package com.qa.email.utils;

import com.mailosaur.models.Link;
import com.qa.email.model.EmailLink;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Helpers for finding and validating links found in emails. */
public final class LinkUtils {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private LinkUtils() {
    }

    /** Converts Mailosaur SDK links to our SDK-independent type. */
    public static List<EmailLink> from(List<Link> links) {
        if (links == null) {
            return List.of();
        }
        return links.stream().map(l -> new EmailLink(l.text(), l.href())).collect(Collectors.toList());
    }

    public static Optional<EmailLink> findByText(List<EmailLink> links, String textFragment) {
        return links.stream()
                .filter(l -> l.text().toLowerCase().contains(textFragment.toLowerCase()))
                .findFirst();
    }

    public static Optional<EmailLink> findByHref(List<EmailLink> links, String hrefFragment) {
        return links.stream()
                .filter(l -> l.href().toLowerCase().contains(hrefFragment.toLowerCase()))
                .findFirst();
    }

    /** Links that are not https (mailto: and tel: are ignored). */
    public static List<EmailLink> insecureLinks(List<EmailLink> links) {
        return links.stream()
                .filter(l -> l.href().toLowerCase().startsWith("http://"))
                .collect(Collectors.toList());
    }

    /**
     * Issues a GET (following redirects) and returns the final HTTP status.
     * Returns -1 when the host cannot be reached at all.
     */
    public static int httpStatus(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            return HTTP.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (IOException | IllegalArgumentException e) {
            return -1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    /** Only http(s) links are worth probing; mailto:, tel: and anchors are skipped. */
    public static boolean isWebLink(EmailLink link) {
        String h = link.href().toLowerCase();
        return h.startsWith("http://") || h.startsWith("https://");
    }
}
