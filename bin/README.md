# Email Test Automation Framework

Java 17 · Maven · TestNG · Mailosaur · AssertJ · Jsoup · Extent Reports · Log4j2

Tests that verify the emails your application sends: subject, sender, body, links, attachments and OTP codes.

## Structure

```
src/main/java/com/qa/email
  config/ConfigManager      env var > -D property > config-{env}.properties > config.properties
  service/EmailService      the only class that talks to Mailosaur (wait, fetch, download, clear)
  assertions/EmailAssert    fluent assertions: hasSubject, sentFrom, hasAttachment, allImagesHaveAltText...
  utils/OtpExtractor        safe OTP/code extraction (no IllegalStateException on no-match)
  utils/LinkUtils           find links, flag http://, check reachability
  model/EmailLink           SDK-independent link type (unit-testable)
src/test/java/com/qa/email
  base/BaseEmailTest        obtains the email under test; skips cleanly without credentials
  support/AppEmailTrigger   how the app under test is told to send an email (customise this)
  tests/                    EmailContentTest, EmailLinkTest, EmailAttachmentTest, OtpEmailTest
  unit/                     offline tests for the utilities
  reporting/                TestListener + ExtentManager -> target/reports/email-test-report.html
src/test/resources          testng.xml (all), smoke.xml, unit.xml
```

## Setup

1. Put credentials in the environment (never in git):
   ```bash
   export MAILOSAUR_API_KEY=your-api-key
   export MAILOSAUR_SERVER_ID=your-server-id
   ```
   or in a git-ignored `src/main/resources/config-local.properties`:
   ```properties
   mailosaur.api.key=...
   mailosaur.server.id=...
   ```
2. Choose how a test gets an email (see `config.properties`):
   - **Trigger mode** – set `app.trigger.url`; the framework generates a unique recipient per test and POSTs `{"email": "..."}` to it. Safe for parallel runs and re-runs. Replace `AppEmailTrigger.send` to use a UI step or an API client instead.
   - **Fallback mode** – leave `app.trigger.url` empty; tests read an email that is already waiting for `anything@<server>.mailosaur.net` (the workflow of the original script).
3. Fill in the `expect.*` values. A blank value makes that test **skip**, not fail.

## Run

```bash
mvn test                          # everything (integration skips if no credentials)
mvn test -Dsuite=unit.xml         # offline utilities only
mvn test -Dsuite=smoke.xml        # fast gate
mvn test -Denv=staging            # loads config-staging.properties on top of config.properties
```

Reports: `target/reports/email-test-report.html`, logs: `target/logs/email-tests.log`, downloaded attachments: `target/downloads/`.

## Adding a test

```java
public class WelcomeEmailTest extends BaseEmailTest {
    @Test(groups = {"integration"})
    public void welcomeEmailHasVerifyLink() throws Exception {
        Message m = obtainEmail();
        assertThatEmail(m).subjectContains("Welcome").hasAtLeastLinks(1);
        assertThat(LinkUtils.findByText(emails.links(m), "Verify")).isPresent();
    }
}
```

## Conventions

- One recipient per test (`emails.newAddress("prefix")`) – no shared-inbox races.
- Tests call `EmailService` / `EmailAssert`, never the Mailosaur SDK directly.
- Groups: `unit`, `integration`, `smoke`, `extended` (slower checks such as link reachability).

## Ideas for next steps

Spam-score analysis, rendering checks across email clients, a Selenium page object feeding `AppEmailTrigger`, RestAssured-based triggers, Allure instead of Extent.
