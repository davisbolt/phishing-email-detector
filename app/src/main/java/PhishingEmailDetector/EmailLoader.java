package PhishingEmailDetector;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class EmailLoader {
    private static final EmailLoader emailLoader = new EmailLoader();

    public static EmailLoader getInstance() {
        return emailLoader;
    }

    public String[] loadEmails(String path) {
        String[] emails = null;
        try {
            if ((new File(path)).isDirectory()) {
                File[] files = (new File(path)).listFiles();
                emails = new String[files.length];

                for (int i = 0; i < files.length - 1; i++) {
                    FileInputStream fis = new FileInputStream(files[i]);
                    byte[] data = new byte[(int) files[i].length()];
                    fis.read(data);
                    emails[i] = new String(data, StandardCharsets.UTF_8);
                }
            } else if ((new File(path).isFile())) {
                File file = new File(path);
                emails = new String[1];

                FileInputStream fis = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                emails[0] = new String(data, StandardCharsets.UTF_8);
            } else {
                System.out.println("Not a valid file path or directory");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return emails;
    }
}
