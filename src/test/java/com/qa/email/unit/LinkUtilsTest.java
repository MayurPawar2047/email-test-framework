package com.qa.email.unit;

import com.qa.email.model.EmailLink;
import com.qa.email.utils.LinkUtils;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class LinkUtilsTest {

    private final List<EmailLink> links = List.of(
            new EmailLink("Verify account", "https://app.example.com/verify?token=abc"),
            new EmailLink("Unsubscribe", "http://app.example.com/unsub"),
            new EmailLink("Support", "mailto:help@example.com"));

    @Test(groups = "unit")
    public void findsLinkByVisibleText() {
        assertThat(LinkUtils.findByText(links, "verify")).isPresent();
    }

    @Test(groups = "unit")
    public void findsLinkByHrefFragment() {
        assertThat(LinkUtils.findByHref(links, "token=abc")).isPresent();
        assertThat(LinkUtils.findByHref(links, "does-not-exist")).isEmpty();
    }

    @Test(groups = "unit")
    public void detectsInsecureLinks() {
        assertThat(LinkUtils.insecureLinks(links)).extracting(EmailLink::text).containsExactly("Unsubscribe");
    }

    @Test(groups = "unit")
    public void mailtoIsNotAWebLink() {
        assertThat(LinkUtils.isWebLink(links.get(2))).isFalse();
        assertThat(LinkUtils.isWebLink(links.get(0))).isTrue();
    }
}
