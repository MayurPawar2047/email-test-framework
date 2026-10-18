package com.qa.email.service;

import com.mailosaur.MailosaurClient;
import com.mailosaur.MailosaurException;
import com.mailosaur.models.Attachment;
import com.mailosaur.models.Message;
import com.mailosaur.models.MessageSearchParams;
import com.mailosaur.models.SearchCriteria;
import com.qa.email.config.ConfigManager;
import com.qa.email.model.EmailLink;
import com.qa.email.utils.LinkUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Thin service layer over the Mailosaur SDK. Tests talk to this class only,
 * so a change of provider (or SDK upgrade) touches one file.
 */
public class EmailService {

    private static final Logger LOG = LogManager.getLogger(EmailService.class);

    private final MailosaurClient client;
    private final String serverId;
    private final String serverDomain;
    private final int timeoutMs;

    public EmailService() {
        this.serverId = ConfigManager.get("mailosaur.server.id");
        this.serverDomain = ConfigManager.get("mailosaur.server.domain", serverId + ".mailosaur.net");
        this.timeoutMs = ConfigManager.getInt("email.timeout.ms", 20_000);
        this.client = new MailosaurClient(ConfigManager.get("mailosaur.api.key"));
    }

    // ------------------------------------------------------------------ addresses

    /** Unique recipient per test - makes parallel runs and re-runs collision-free. */
    public String newAddress(String prefix) {
        String address = prefix + "." + UUID.randomUUID().toString().substring(0, 8) + "@" + serverDomain;
        LOG.info("Generated test address {}", address);
        return address;
    }

    public String fixedAddress(String localPart) {
        return localPart + "@" + serverDomain;
    }

    // ------------------------------------------------------------------ retrieval

    /** Waits (up to email.timeout.ms) for the most recent email sent to the address. */
    public Message waitForEmail(String toAddress) throws IOException, MailosaurException {
        return search(new SearchCriteria().withSentTo(toAddress));
    }

    /** Same as above, additionally narrowed by a subject fragment. */
    public Message waitForEmail(String toAddress, String subjectContains)
            throws IOException, MailosaurException {
        return search(new SearchCriteria().withSentTo(toAddress).withSubject(subjectContains));
    }

    private Message search(SearchCriteria criteria) throws IOException, MailosaurException {
        // withReceivedAfter(...) is deliberately not used: its argument type differs between
        // SDK versions (Date / OffsetDateTime / long). Mailosaur's default window is the last hour.
        MessageSearchParams params = new MessageSearchParams();
        params.withServer(serverId);
        params.withTimeout(timeoutMs);

        LOG.info("Waiting up to {} ms for email (server={})", timeoutMs, serverId);
        Message message = client.messages().get(params, criteria);
        LOG.info("Received email '{}' (id={})", message.subject(), message.id());
        return message;
    }

    // ------------------------------------------------------------------ content helpers

    public List<EmailLink> links(Message message) {
        return LinkUtils.from(message.html().links());
    }

    public String textBody(Message message) {
        return message.text() == null ? "" : String.valueOf(message.text().body());
    }

    public String htmlBody(Message message) {
        return message.html() == null ? "" : String.valueOf(message.html().body());
    }

    // ------------------------------------------------------------------ attachments

    public byte[] downloadAttachment(Attachment attachment) throws IOException, MailosaurException {
        return client.files().getAttachment(attachment.id());
    }

    /** Downloads the attachment into {@code dir} and returns the saved file. */
    public Path saveAttachment(Attachment attachment, Path dir) throws IOException, MailosaurException {
        Files.createDirectories(dir);
        Path target = dir.resolve(attachment.fileName());
        Files.write(target, downloadAttachment(attachment));
        LOG.info("Saved attachment {} to {}", attachment.fileName(), target.toAbsolutePath());
        return target;
    }

    // ------------------------------------------------------------------ housekeeping

    public void clearInbox() throws IOException, MailosaurException {
        LOG.info("Deleting all messages on server {}", serverId);
        client.messages().deleteAll(serverId);
    }

    public void delete(Message message) throws IOException, MailosaurException {
        client.messages().delete(message.id());
    }
}