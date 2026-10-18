package com.qa.email.assertions;

import com.mailosaur.models.Attachment;
import com.mailosaur.models.Message;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** Fluent, readable assertions for a received email. */
public final class EmailAssert {

    private final Message message;

    private EmailAssert(Message message) {
        this.message = message;
    }

    public static EmailAssert assertThatEmail(Message message) {
        assertThat(message).as("email was received").isNotNull();
        return new EmailAssert(message);
    }

    public EmailAssert hasSubject(String expected) {
        assertThat(message.subject()).as("subject").isEqualTo(expected);
        return this;
    }

    public EmailAssert subjectContains(String fragment) {
        assertThat(message.subject()).as("subject").contains(fragment);
        return this;
    }

    public EmailAssert sentTo(String address) {
        assertThat(message.to()).as("recipients").isNotEmpty();
        assertThat(message.to().get(0).email()).as("first recipient").isEqualToIgnoringCase(address);
        return this;
    }

    public EmailAssert sentFrom(String address) {
        assertThat(message.from()).as("senders").isNotEmpty();
        assertThat(message.from().get(0).email()).as("sender").isEqualToIgnoringCase(address);
        return this;
    }

    public EmailAssert textBodyContains(String fragment) {
        assertThat(String.valueOf(message.text().body())).as("plain-text body").contains(fragment);
        return this;
    }

    public EmailAssert htmlBodyContains(String fragment) {
        assertThat(String.valueOf(message.html().body())).as("HTML body").contains(fragment);
        return this;
    }

    public EmailAssert hasAtLeastLinks(int count) {
        assertThat(message.html().links()).as("links in HTML body").hasSizeGreaterThanOrEqualTo(count);
        return this;
    }

    public EmailAssert hasAttachmentCount(int count) {
        assertThat(message.attachments()).as("attachments").hasSize(count);
        return this;
    }

    public EmailAssert hasAttachment(String fileName, String contentType) {
        List<Attachment> match = message.attachments().stream()
                .filter(a -> fileName.equalsIgnoreCase(a.fileName()))
                .collect(Collectors.toList());
        assertThat(match).as("attachment named " + fileName).isNotEmpty();
        assertThat(match.get(0).contentType()).as("content type of " + fileName).isEqualTo(contentType);
        return this;
    }

    /** Accessibility check: every <img> in the HTML body should carry an alt attribute. */
    public EmailAssert allImagesHaveAltText() {
        Document doc = Jsoup.parse(String.valueOf(message.html().body()));
        List<String> missing = doc.select("img").stream()
                .filter(img -> !img.hasAttr("alt"))
                .map(Element::outerHtml)
                .collect(Collectors.toList());
        assertThat(missing).as("images without alt attribute").isEmpty();
        return this;
    }
}
