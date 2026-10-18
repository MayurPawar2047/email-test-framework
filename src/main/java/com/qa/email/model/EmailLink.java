package com.qa.email.model;

/**
 * Lightweight, SDK-independent view of a hyperlink in an email.
 * Keeping our own type means link logic can be unit-tested without Mailosaur.
 */
public record EmailLink(String text, String href) {

    public EmailLink {
        text = text == null ? "" : text.trim();
        href = href == null ? "" : href.trim();
    }

    public boolean isHttps() {
        return href.toLowerCase().startsWith("https://");
    }

    @Override
    public String toString() {
        return "[" + text + "] -> " + href;
    }
}
