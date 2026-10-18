package com.qa.email.tests;

import com.mailosaur.models.Message;
import com.qa.email.base.BaseEmailTest;
import org.testng.annotations.Test;

import static com.qa.email.assertions.EmailAssert.assertThatEmail;

public class EmailContentTest extends BaseEmailTest {

    @Test(groups = {"integration", "smoke"}, description = "Email arrives with the expected subject")
    public void subjectMatches() throws Exception {
        Message message = obtainEmail();
        assertThatEmail(message).hasSubject(requireConfig("expect.subject"));
    }

    @Test(groups = {"integration"}, description = "Sender is the expected address")
    public void senderMatches() throws Exception {
        String expectedSender = requireConfig("expect.sender");
        assertThatEmail(obtainEmail()).sentFrom(expectedSender);
    }

    @Test(groups = {"integration"}, description = "Plain-text body contains the expected text")
    public void bodyContainsExpectedText() throws Exception {
        String expectedText = requireConfig("expect.body.text");
        assertThatEmail(obtainEmail()).textBodyContains(expectedText);
    }

    @Test(groups = {"integration"}, description = "Every image in the HTML body has alt text")
    public void imagesHaveAltText() throws Exception {
        assertThatEmail(obtainEmail()).allImagesHaveAltText();
    }
}
