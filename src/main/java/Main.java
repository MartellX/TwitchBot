import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;


import java.io.*;
import java.security.GeneralSecurityException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException, GeneralSecurityException {
        OAuth2Credential twitchCredential = new OAuth2Credential("***REMOVED***",
                "x45opnw0dsdy37ll44me2wlb1nyhu0");
        String googleCredsPath = "./src/main/resources/Quickstart-88b91e2083dc.json";

        MainController.setTwitchBot(twitchCredential);
        MainController.setGoogleSheets(googleCredsPath);
        MainController.setLogsFile("logs.txt");
        MainController.joinTo("martellx");
        //MainController.joinTo("uselessmouth");
    }
}
