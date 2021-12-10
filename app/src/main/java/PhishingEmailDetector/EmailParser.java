package PhishingEmailDetector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.validator.routines.*;

public class EmailParser {
    private String[] emails;
    private int sum = 0;

    public EmailParser(String path){
        emails = EmailLoader.getInstance().loadEmails(path);
    }

    public void testEmails(){
        if (emails == null ) return;

        for (String email : emails) {
            //sum += getTestScores(email);
            if (getTestScores(email) >= 1){
                sum++;
            }
        }
        System.out.println(sum);
    }

    private int getTestScores(String email){
        int numTestsPassed = 0;

        if (hasMisleadingLinks(email)) numTestsPassed++;
        if (hasRedirectingLinks(email)) numTestsPassed++;
        if (hasUnsafeLinks(email)) numTestsPassed++;

        if (hasDomainNameMismatch(email)) numTestsPassed++;
        if (hasReturnAddressMismatch(email)) numTestsPassed++;

        if (hasSuspiciousAttachment(email)) numTestsPassed++;

        //if (hasSpellingErrors(email)) numTestsPassed++;

        return numTestsPassed;
    }

    private boolean hasMisleadingLinks(String email){
        ArrayList<String> linkTags = getLinkTags(email);
        for (String linkTag : linkTags){
            Pattern displayedLinkPattern = Pattern.compile(">[^<>]+<");
            Matcher displayedLinkMatcher = displayedLinkPattern.matcher(linkTag);
            while (displayedLinkMatcher.find()){
                String displayedLink = displayedLinkMatcher.group().replaceAll("[ \t\n\\x0B\f\r<>]", "");
                UrlValidator validator = new UrlValidator();
                if (validator.isValid(displayedLink) && !displayedLink.equalsIgnoreCase(getHref(linkTag)))
                    return true;
            }
        }

        return false;
    }

    private boolean hasRedirectingLinks(String email){
        ArrayList<String> linkTags = getLinkTags(email);
        for (String linkTag : linkTags){
            if (getHref(linkTag) != null && getHref(linkTag).contains("/do/redirect"))
                return true;
        }

        return false;
    }

    private boolean hasUnsafeLinks(String email){
        ArrayList<String> linkTags = getLinkTags(email);
        for (String linkTag : linkTags){
            if (getHref(linkTag) != null && getHref(linkTag).contains("http:"))
                return true;
        }

        return false;
    }

    private boolean hasDomainNameMismatch(String email){
        ArrayList<String> linkTags = getLinkTags(email);
        String fromAddress = getFromAddress(email);
        String addressDomain = null;
        String linkDomain = null;

        if(fromAddress != null)
            addressDomain = fromAddress.substring(fromAddress.indexOf('@') + 1);

        for (String linkTag : linkTags){
            String href = getHref(linkTag);
            UrlValidator urlValidator = new UrlValidator();
            if (urlValidator.isValid(href)) {
                try {
                    URI linkUri = new URI(href);
                    linkDomain = linkUri.getHost().replaceFirst("www.", "");
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            if (addressDomain != null && linkDomain != null && !linkDomain.contains(addressDomain))
                return true;
        }

        return false;
    }

    private boolean hasReturnAddressMismatch(String email){
        String fromAddress = getFromAddress(email);
        String returnAddress = getReturnAddress(email);

        return fromAddress != null && returnAddress != null && !fromAddress.equals(returnAddress);
    }

    private boolean hasSuspiciousAttachment(String email){
        Pattern attachmentPattern = Pattern.compile("Content-Disposition: attachment; filename=[^\n]*");
        Matcher attachmentMatcher = attachmentPattern.matcher(email);
        if (attachmentMatcher.find()){
            String attachment = attachmentMatcher.group();
            String attachmentClean = attachment.replaceAll("Content-Disposition: attachment; filename=|[\n\" ]", "");
            Pattern extensionPattern = Pattern.compile("(.exe|.zip|.htm|.html)$", Pattern.CASE_INSENSITIVE);
            if (extensionPattern.matcher(attachmentClean).find())
                return true;
        }

        return false;
    }

    private boolean hasSpellingErrors(String email){

        return false;
    }



    private ArrayList<String> getLinkTags(String email){
        ArrayList<String> linkTags = new ArrayList<>();
        Pattern linkTagPattern = Pattern.compile("<a [^>]*>((?!</a>).)*</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher linkTagMatcher = linkTagPattern.matcher(email);
        while (linkTagMatcher.find()){
            linkTags.add(linkTagMatcher.group());
        }
        return linkTags;
    }

    private String getHref(String linkTag){
        Pattern hrefPattern = Pattern.compile("href=[^\"]*\"[^\"<>]*\"", Pattern.CASE_INSENSITIVE);
        Matcher hrefMatcher = hrefPattern.matcher(linkTag);
        if (hrefMatcher.find()) {
            String href = hrefMatcher.group();
            return href.substring(href.indexOf('\"') + 1, href.length() - 1).replaceAll("[ \t\n\\x0B\f\r]", "").toLowerCase();
        }
        return null;
    }

    private String getFromAddress(String email){
        Pattern addressPattern = Pattern.compile("From:[^<\n]*<[^>\n]*>", Pattern.CASE_INSENSITIVE);
        Matcher addressMatcher = addressPattern.matcher(email);
        if (addressMatcher.find()){
            String address = addressMatcher.group();
            String addressClean = address.substring(address.indexOf('<') + 1, address.length() - 1).replaceAll("[ \"]", "").toLowerCase();
            if (EmailValidator.getInstance().isValid(addressClean))
                return addressClean;
        }

        return null;
    }

    private String getReturnAddress(String email){
        Pattern addressPattern = Pattern.compile("Return-Path:[^\n]*", Pattern.CASE_INSENSITIVE);
        Matcher addressMatcher = addressPattern.matcher(email);
        if (addressMatcher.find()){
            String address = addressMatcher.group();
            String addressClean = address.replaceAll("(Return-Path:|[><\" ])", "").toLowerCase();
            if (EmailValidator.getInstance().isValid(addressClean))
                return addressClean;
        }

        return null;
    }
}
