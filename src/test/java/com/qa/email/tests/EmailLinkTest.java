package com.qa.email.tests;

import com.mailosaur.models.Message;
import com.qa.email.base.BaseEmailTest;
import com.qa.email.model.EmailLink;
import com.qa.email.utils.LinkUtils;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class EmailLinkTest extends BaseEmailTest {

    @Test(groups = {"integration"}, description = "Email contains the expected call-to-action link")
    public void containsExpectedLink() throws Exception {
        String fragment = requireConfig("expect.link.contains");
        Message message = obtainEmail();

        List<EmailLink> links = emails.links(message);
        assertThat(LinkUtils.findByHref(links, fragment))
                .as("link whose href contains '%s' among %s", fragment, links)
                .isPresent();
    }

    @Test(groups = {"integration"}, description = "No link in the email uses plain http")
    public void noInsecureLinks() throws Exception {
        List<EmailLink> links = emails.links(obtainEmail());
        assertThat(LinkUtils.insecureLinks(links)).as("http:// links").isEmpty();
    }

    @Test(groups = {"integration", "extended"}, description = "Every web link resolves without a 4xx/5xx")
    public void linksAreReachable() throws Exception {
        List<EmailLink> links = emails.links(obtainEmail()).stream()
                .filter(LinkUtils::isWebLink)
                .collect(Collectors.toList());

        List<String> broken = links.stream()
                .filter(l -> {
                    int status = LinkUtils.httpStatus(l.href());
                    return status < 0 || status >= 400;
                })
                .map(EmailLink::toString)
                .collect(Collectors.toList());

        assertThat(broken).as("broken links").isEmpty();
    }
}
