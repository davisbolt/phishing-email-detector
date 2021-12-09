package PhishingEmailDetector;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.validator.routines.*;

public class EmailParser {
    private String[] emails;
    private int sum;

    public EmailParser(String path){
        emails = EmailLoader.getInstance().loadEmails(path);
    }

    public void testEmails(){
        if (emails == null ) return;

        for (String email : emails) {
            sum += getTestScores(email);
        }
        System.out.println(sum);
    }

    private int getTestScores(String email){
        int numTestsPassed = 0;

        if (hasMisleadingLinks(email)) numTestsPassed++;
        if (hasDomainNameMismatch(email)) numTestsPassed++;
        if (hasRedirectingLinks(email)) numTestsPassed++;
        if (hasReturnAddressMismatch(email)) numTestsPassed++;
        if (hasSuspiciousAttachment(email)) numTestsPassed++;
        if (hasSpellingErrors(email)) numTestsPassed++;

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
                if (validator.isValid(displayedLink) && !displayedLink.equals(getHref(linkTag)))
                    return true;
            }
        }

        return false;
    }

    private boolean hasDomainNameMismatch(String email){

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
    private boolean hasReturnAddressMismatch(String email){

        return false;
    }

    private boolean hasSuspiciousAttachment(String email){

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
        Pattern hrefPattern = Pattern.compile("href=[^\"]*\"[^\"<>]*\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher hrefMatcher = hrefPattern.matcher(linkTag);
        if (hrefMatcher.find()) {
            String href = hrefMatcher.group();
            return href.substring(href.indexOf('\"') + 1, href.length() - 1).replaceAll("[ \t\n\\x0B\f\r]", "");
        }
        return null;
    }
}
