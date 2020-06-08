import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import constants.CommandConstants;
import controllers.MainController;


import java.io.*;
import java.security.GeneralSecurityException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException, GeneralSecurityException {
        CommandConstants.init();
        OAuth2Credential twitchCredential = new OAuth2Credential("***REMOVED***",
                "***REMOVED***");
        String googleCredsPath = "./src/main/resources/Quickstart-88b91e2083dc.json";


        MainController.setTwitchBot(twitchCredential);
        MainController.setGoogleSheets(googleCredsPath);
        MainController.setLogsFile("logs.txt");
        MainController.joinTo("martellx");
        //MainController.joinTo("uselessmouth");
    }
}
