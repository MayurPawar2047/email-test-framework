package com.qa.email.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts one-time passwords / access codes from email text.
 *
 * Unlike a bare {@code matcher.find(); matcher.group(1)}, every method here
 * handles "no match" explicitly instead of throwing IllegalStateException.
 * The regex must contain exactly one capturing group - the code itself.
 */
public final class OtpExtractor {

    /** Any standalone 6-digit number, e.g. "123456". */
    public static final Pattern SIX_DIGIT = Pattern.compile("\\b(\\d{6})\\b");

    /** "your otp is 12345678" style sentence with an 8-digit code (case-insensitive). */
    public static final Pattern OTP_SENTENCE_8_DIGIT =
            Pattern.compile("(?i)your otp is\\D*(\\d{8})");

    /** Two letters followed by six digits, e.g. "QZ524822" (note: no space between them). */
    public static final Pattern TWO_LETTERS_SIX_DIGITS = Pattern.compile("\\b([A-Z]{2}\\d{6})\\b");

    private OtpExtractor() {
    }

    public static Optional<String> find(String text, Pattern pattern) {
        if (text == null || pattern == null) {
            return Optional.empty();
        }
        Matcher m = pattern.matcher(text);
        return m.find() ? Optional.of(m.group(1)) : Optional.empty();
    }

    public static Optional<String> find(String text, String regex) {
        return find(text, Pattern.compile(regex));
    }

    /** Same as {@link #find} but fails the test with a readable message when nothing matches. */
    public static String require(String text, Pattern pattern) {
        return find(text, pattern).orElseThrow(() -> new AssertionError(
                "No code matching /" + pattern.pattern() + "/ found in email body:\n" + text));
    }

    public static String require(String text, String regex) {
        return require(text, Pattern.compile(regex));
    }
}
