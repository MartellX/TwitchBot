import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {

    static private TwitchBot twitchBot;
    static private GoogleSheets googleSheets;
    static private File logsFile;
    static private Map<String, List<String>> lastEvents;
    static private Map<String, Integer> allPastes;
    static private List<String> nicks;

    static private ChatBot chatBot = new ChatBot();
    static private ComicBot comicBot = new ComicBot();
    static private HttpClientController httpClient = new HttpClientController();

    static private int maxPastCount = 5;


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
        int startGGP = 0;
        double finalGGP = 0;

        if (infoMap.containsKey("Range") && !infoMap.get("Range").equals("")) {
            sbInfo.append("Отрезок: " + infoMap.get("Range") + ", ");
        }
        if (infoMap.containsKey("Game") && !infoMap.get("Game").equals("")) {
            sbInfo.append("Игра: " + infoMap.get("Game") + ", ");
            sbInfo.append("Время прохождения: [" + httpClient.getGameTimeFromHLTB(infoMap.get("Game")) + "], ");
        }
        if (infoMap.containsKey("GGP") && !infoMap.get("GGP").equals("")) {
            startGGP = Integer
                    .parseInt(infoMap
                            .get("GGP")
                            .replaceAll("[\\[\\]]", ""));
            finalGGP = startGGP;
        }

        if (infoMap.containsKey("Events") && !infoMap.get("Events").equals("")) {
            String events = infoMap.get("Events");
            Pattern pattern = Pattern.compile("Бухгалтерия \\(\\d*\\)");
            Matcher matcher = pattern.matcher(events);
            while (matcher.find()){
                String last = matcher.group();
                last = last.replaceAll("[^\\d]", "");
                Integer newGGP = Integer.parseInt(last);
                startGGP = newGGP;
                finalGGP = newGGP;
            }

            pattern = Pattern.compile("[-−]\\d*%");
            matcher = pattern.matcher(events);
            double percent = 0;
            while (matcher.find()) {
                String match = matcher.group();
                match = match.replaceAll("[^\\d]", "");
                percent -= Integer.parseInt(match);

            }

            pattern = Pattern.compile("\\+\\d*%");
            matcher = pattern.matcher(events);
            while(matcher.find()){
                String match = matcher.group();
                match = match.replaceAll("[^\\d]", "");
                percent += Integer.parseInt(match);

            }

            double k = (100 + percent) / 100;
            finalGGP = finalGGP * k;

            pattern = Pattern.compile("\\+\\d*\\sGGP");
            matcher = pattern.matcher(events);
            while(matcher.find()){
                String match = matcher.group();
                match = match.replaceAll("[^\\d]", "");
                double plusGGP = Integer.parseInt(match);
                finalGGP += plusGGP;
            }
        }

        sbInfo.append("Номинальное GGP: " + startGGP + ", Итоговое GGP: " + (int)finalGGP + ", ");

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
        blacklist.add("п***р");
        blacklist.add("п****");
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
            allPastes.clear();
            /*
            for (Map.Entry<String, Integer> e: allPastes.entrySet()
                 ) {
                if (e.getValue() < 3) {
                    allPastes.remove(e);
                }
            }

             */
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

    static public void handleNick(String nick) {
        if (nicks == null) {
            nicks = new ArrayList<>();
        }

        if (!nicks.contains(nick)) {
            nicks.add(nick);
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

    static public String getAnswerFromChatbot(String msg){
        String answer = chatBot.getAnswer(msg);
        return answer;
    }

    static public String getAnswerFromComicbot() {
        String answer = comicBot.getAnek();
        return answer;
    }

    static private String[] whenAnswers = {"сейчас", "завтра", "вчера", "через полчаса",
            "никогда SadCat", "всегда widepeepoHappy", "через неделю", "через год", "когда юзя видеокарту разгонит",
            "а хуй знает"};
    static public String when() {
        Random rd = new Random();
        String answer = whenAnswers[rd.nextInt(whenAnswers.length)];
        return answer;
    }

    static private String[] whoAnswers = {"он", "ты", "я", "никто",
            "все", "кто-то", "чат"};

    static public String who() {
        Random rd = new Random();
        StringBuilder answ = new StringBuilder(whoAnswers[rd.nextInt(whoAnswers.length)]);
        if (answ.toString().equals("он")) {
            answ.append(" :point_right: @" + nicks.get(rd.nextInt(nicks.size())));
        }

        return answ.toString();
    }

    static private String[] whereAnswers = {"там :point_right:", "там :point_up:",
            "там :point_down:", "там :point_left:", "сзади monkaW", "за окном", "нигде",
    "хз, поищи"};
    static public String where() {
        Random rd = new Random();
        String answ = whereAnswers[rd.nextInt(whereAnswers.length)];
        return answ;
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
