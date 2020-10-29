package constants;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private static List<String> envVariables = List.of("TWITCH_NICKNAME", "TWITCH_ATOKEN", "TWITCH_CID",
            "GOOGLE_CREDS", "DATABASE_URL",
            "RECOGNIZING_HOST", "RECOGNIZING_AKEY", "RECOGNIZING_SKEY",
            "UNOFFICIAL_TWITCH_CID", "UNOFFICIAL_TWITCH_OAUTH");
    private static Map<String, String> configMap = new HashMap<>();
    static {
        for (String var:envVariables
             ) {
            String variable = System.getenv(var);
            if (variable == null) {
                System.out.println("[ERROR]Не получилось получить" + var);
            } else {
                configMap.put(var, variable);
            }
        }
    }

    public static String getStringFor(String key){
        String result = configMap.get(key);
        if (result == null) {
            throw new NullPointerException("Не удалось получить " + key);
        } else {
            return result;
        }
    }


}
