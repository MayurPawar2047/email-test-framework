package com.qa.email.tests;

import com.mailosaur.models.Message;
import com.qa.email.base.BaseEmailTest;
import com.qa.email.utils.OtpExtractor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OtpEmailTest extends BaseEmailTest {

    @Test(groups = {"integration", "smoke"}, description = "OTP can be extracted from the email body")
    public void otpIsPresent() throws Exception {
        String regex = requireConfig("expect.otp.regex");
        Message message = obtainEmail();

        String otp = OtpExtractor.require(emails.textBody(message), regex);

        assertThat(otp).as("extracted OTP").isNotBlank();
    }
}
