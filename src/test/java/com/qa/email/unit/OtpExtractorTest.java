package com.qa.email.unit;

import com.qa.email.utils.OtpExtractor;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Offline tests - no Mailosaur account needed. */
public class OtpExtractorTest {

    @Test(groups = "unit")
    public void extractsEightDigitOtpFromSentence() {
        String body = "Hello Mayur, Your OTP is: 12345678. It expires in 10 minutes.";
        assertThat(OtpExtractor.find(body, OtpExtractor.OTP_SENTENCE_8_DIGIT)).contains("12345678");
    }

    @Test(groups = "unit")
    public void extractsTwoLettersSixDigitsCode() {
        String body = "Your access code is QZ524822.";
        assertThat(OtpExtractor.find(body, OtpExtractor.TWO_LETTERS_SIX_DIGITS)).contains("QZ524822");
    }

    @Test(groups = "unit")
    public void extractsSixDigitCode() {
        assertThat(OtpExtractor.find("Use 987654 to sign in", OtpExtractor.SIX_DIGIT)).contains("987654");
    }

    @Test(groups = "unit")
    public void returnsEmptyInsteadOfThrowingWhenNoMatch() {
        assertThat(OtpExtractor.find("no code here", OtpExtractor.SIX_DIGIT)).isEmpty();
        assertThat(OtpExtractor.find(null, OtpExtractor.SIX_DIGIT)).isEmpty();
    }

    @Test(groups = "unit")
    public void requireFailsWithReadableMessage() {
        assertThatThrownBy(() -> OtpExtractor.require("nothing", OtpExtractor.SIX_DIGIT))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("No code matching");
    }
}
