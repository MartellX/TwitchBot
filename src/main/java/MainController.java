import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class MainController {

    static private TwitchBot twitchBot;
    static private GoogleSheets googleSheets;
    static private File logsFile;
    static private Map<String, List<String>> lastEvents;
    static private Map<String, Integer> allPastes;

    static private int maxPastCount = 5;
    static private String[] whenAnswers = {"сейчас", "завтра", "вчера", "через полчаса",
            "никогда SadCat", "всегда widepeepoHappy", "через неделю", "через год", "когда юзя видеокарту разгонит",
            "а хуй знает"};

    static public void setTwitchBot(OAuth2Credential twitchCreds) {
        twitchBot = new TwitchBot(twitchCreds);
    }

    static public void setGoogleSheets(String googleCreds) throws GeneralSecurityException, IOException {
        googleSheets = new GoogleSheets(googleCreds);
    }

    static public void setLogsFile(String logsPath) {
        logsFile = new File(logsPath);
    }

    static public String getTop() {
        StringBuilder sbTop = new StringBuilder();
        List<List<Object>> topList = googleSheets.getTop();

        for (int i = 0; i < topList.size(); i++) {
            sbTop.append(String.format("%s. %s - %s", topList.get(i).get(0), topList.get(i).get(1), topList.get(i).get(8)));
            if (i != topList.size() - 1) {
                sbTop.append("; ");
            }
        }

        return sbTop.toString();
    }

    static public String getInfoAbout(String nick) {
        StringBuilder sbInfo = new StringBuilder();
        Map<String, String> infoMap = googleSheets.getInfoAbout(nick);


        if (infoMap.containsKey("Range") && !infoMap.get("Range").equals("")) {
            sbInfo.append("Отрезок: " + infoMap.get("Range") + ", ");
        }
        if (infoMap.containsKey("Game") && !infoMap.get("Game").equals("")) {
            sbInfo.append("Игра: " + infoMap.get("Game") + ", ");
        }
        if (infoMap.containsKey("GGP") && !infoMap.get("GGP").equals("")) {
            sbInfo.append("Номинальное GGP: " + infoMap.get("GGP") + ", ");
        }
        if (infoMap.containsKey("Comment") && !infoMap.get("Comment").equals("")) {
            sbInfo.append("Комментарий: " + infoMap.get("Comment") + ", ");
        }

        sbInfo.delete(sbInfo.length() - 2, sbInfo.length());
        return sbInfo.toString();
    }

    static public String updateLastEvent(String nick) {
        if (lastEvents == null) {
            lastEvents = new HashMap<>();
        }
        List<String> lastEventFromData = lastEvents.getOrDefault(nick, null);
        StringBuilder sbLastEvent = new StringBuilder();
        List<String> lastEvent = googleSheets.getLastEvent(nick);
        if (lastEventFromData != null) {
            if(lastEvent == null || CollectionUtils.isEqualCollection(lastEventFromData, lastEvent)) {
                return null;
            }
        }

        sbLastEvent.append("Последнее событие: " + lastEvent.get(0) + ", ");
        sbLastEvent.append("Пояснение: " + lastEvent.get(1));
        lastEvents.put(nick, lastEvent);
        return sbLastEvent.toString();
    }

    static public String getLastEvent(String nick) {
        if (lastEvents.get(nick) != null) {
            List<String> lastEvent = lastEvents.get(nick);
            StringBuilder sbLastEvent = new StringBuilder();
            sbLastEvent.append("Последнее событие: " + lastEvent.get(0) + ", ");
            sbLastEvent.append("Пояснение: " + lastEvent.get(1));
            return sbLastEvent.toString();
        }
        return null;
    }

    static public String getPast() {
        Set<String> blacklist = new HashSet<>();
        blacklist.add("педик");
        blacklist.add("пидорас");
        blacklist.add("пидор");
        blacklist.add("пидрила");
        List<List<Object>> pastes = googleSheets.getPastes();
        Random rand = new Random();
        List<Object> column = pastes.get(rand.nextInt(pastes.size()));
        String past = null;

        while (true) {
            past = column.get(rand.nextInt(column.size())).toString();
            for (String bl:blacklist
                 ) {
                if(past.contains(bl)){
                    continue;
                }
            }
            break;
        }

        return past;
    }

    static public String handleMessage(String message) {
        if (allPastes == null) {
            allPastes = new HashMap<>();
        }

        if (allPastes.size() > 1000) {
            for (Map.Entry<String, Integer> e: allPastes.entrySet()
                 ) {
                if (e.getValue() < 3) {
                    allPastes.remove(e);
                }
            }
        }

        int count = 0;
        String checkedOnCopies = getSubStringIfContains(message);
        if (checkedOnCopies != null) {
            message = checkedOnCopies;
        }

        if (allPastes.containsKey(message)) {
            count = allPastes.get(message);
        }
        allPastes.put(message, ++count);

        if (count > maxPastCount) {
            allPastes.put(message, 0);
            return message;
        } else {
            return null;
        }
    }

    public static String getSubStringIfContains(String string) {
        if (string.length() < 2) {
            return null;
        }
        String specSymbols = "!$()*+.<>?[\\]^{|}";
        StringBuilder substr = new StringBuilder();
        for (int i = 0; i < string.length() / 2; i++) {
            Character chr = string.charAt(i);
            if (specSymbols.contains(chr.toString())) {
                substr.append("\\");
                substr.append(chr);
            } else {
                substr.append(chr);
            }
            String clearedFromSubstrings
                    = string.replaceAll(substr.toString(), "");

            if (clearedFromSubstrings.length() == 0 || clearedFromSubstrings.matches("\\s+")) {
                String returnedString = substr.toString().replaceAll("\\\\?", "");
                return returnedString;
            }
        }

        return null;
    }

    static public String when() {
        Random rd = new Random();
        String answer = whenAnswers[rd.nextInt(whenAnswers.length)];
        return answer;
    }


    static void joinTo (String channel) {
        twitchBot.joinToChannel(channel);
    }

    static void setMaxPastCount(int count) {
        maxPastCount = count;
    }

    static void writeToLogs(String log) {

        try {
            if (!logsFile.exists()) {
                logsFile.createNewFile();
            }
            try (FileWriter fw = new FileWriter(logsFile, true)) {
                fw.append(log + '\n');
            }
        } catch (IOException e) {
            return;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        twitchBot.isClosed = true;
        twitchBot.twitchClient.close();
    }
}
