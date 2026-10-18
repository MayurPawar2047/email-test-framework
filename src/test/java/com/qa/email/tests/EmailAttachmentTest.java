package com.qa.email.tests;

import com.mailosaur.models.Attachment;
import com.mailosaur.models.Message;
import com.qa.email.base.BaseEmailTest;
import com.qa.email.config.ConfigManager;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.qa.email.assertions.EmailAssert.assertThatEmail;
import static org.assertj.core.api.Assertions.assertThat;

public class EmailAttachmentTest extends BaseEmailTest {

    @Test(groups = {"integration"}, description = "Attachment has expected name/type and downloads intact")
    public void attachmentIsDownloadable() throws Exception {
        String expectedName = requireConfig("expect.attachment.name");
        String expectedType = ConfigManager.get("expect.attachment.type", "application/pdf");

        Message message = obtainEmail();
        assertThatEmail(message).hasAttachment(expectedName, expectedType);

        Attachment attachment = message.attachments().stream()
                .filter(a -> expectedName.equalsIgnoreCase(a.fileName()))
                .findFirst()
                .orElseThrow();

        Path saved = emails.saveAttachment(attachment, Paths.get(ConfigManager.get("email.download.dir", "target/downloads")));
        byte[] content = Files.readAllBytes(saved);

        assertThat(content.length).as("downloaded size").isGreaterThan(0);
        if ("application/pdf".equals(expectedType)) {
            byte[] header = Arrays.copyOf(content, 4);
            assertThat(new String(header, StandardCharsets.US_ASCII)).as("PDF magic bytes").isEqualTo("%PDF");
        }
    }
}
