package PhishingEmailDetector;

public class EmailParser {
    private String[] emails;

    public EmailParser(String path){
        emails = EmailLoader.getInstance().loadEmails(path);

        /*for (int i = 0; i < emails.length - 1; i++){
            System.out.println(emails[i]);
        }*/
    }
}
