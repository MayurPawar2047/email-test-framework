package com.qa.email.base;

import com.mailosaur.MailosaurException;
import com.mailosaur.models.Message;
import com.qa.email.config.ConfigManager;
import com.qa.email.service.EmailService;
import com.qa.email.support.AppEmailTrigger;
import org.testng.SkipException;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeClass;

import java.io.IOException;

/**
 * Base for all tests that need a real mailbox.
 * Skips (rather than fails) when Mailosaur credentials are absent, so the
 * offline unit tests stay green on any machine.
 */
public abstract class BaseEmailTest {

    protected EmailService emails;

    @BeforeSuite(alwaysRun = true)
    public void maybeClearInbox() throws IOException, MailosaurException {
        if (ConfigManager.hasMailosaurCredentials() && ConfigManager.getBoolean("email.clear.before.suite", false)) {
            new EmailService().clearInbox();
        }
    }

    @BeforeClass(alwaysRun = true)
    public void initService() {
        if (!ConfigManager.hasMailosaurCredentials()) {
            throw new SkipException("Mailosaur credentials not set "
                    + "(MAILOSAUR_API_KEY / MAILOSAUR_SERVER_ID) - skipping integration tests");
        }
        emails = new EmailService();
    }

    /**
     * Returns the email under test.
     *  - Trigger mode: unique recipient + app sends a fresh email.
     *  - Fallback mode: reads an email already waiting for email.fixed.recipient.
     */
    protected Message obtainEmail() throws Exception {
        if (AppEmailTrigger.isConfigured()) {
            String recipient = emails.newAddress("qa");
            AppEmailTrigger.send(recipient);
            return emails.waitForEmail(recipient);
        }
        return emails.waitForEmail(emails.fixedAddress(ConfigManager.get("email.fixed.recipient", "anything")));
    }

    /** Skips the calling test when an optional expectation is not configured. */
    protected static String requireConfig(String key) {
        return ConfigManager.optional(key).orElseThrow(
                () -> new SkipException("Config '" + key + "' not set - skipping"));
    }
}
