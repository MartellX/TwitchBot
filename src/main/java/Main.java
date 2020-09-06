import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import constants.CommandConstants;
import constants.Config;
import controllers.MainController;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException, GeneralSecurityException {
        CommandConstants.init();
//        OAuth2Credential twitchCredential = new OAuth2Credential("martellx_testbot",
//                "p77qxvmablm14hpvq9xg1mai5sgudw");
      OAuth2Credential twitchCredential = new OAuth2Credential(Config.getStringFor("TWITCH_NICKNAME"),
               Config.getStringFor("TWITCH_ATOKEN"));


        MainController.setTwitchBot(twitchCredential);
        try {
            MainController.setGoogleSheets(Config.getStringFor("GOOGLE_CREDS"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //MainController.setLogsFile("logs.txt");
        MainController.joinTo("martellx");
        //MainController.joinTo("cemka");
        MainController.joinTo("taerss");
    }
}
