package controllers;

import api.*;
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import command.CommandExecutor;
import constants.CommandConstants;
import org.apache.commons.collections4.CollectionUtils;
import utils.FFMpegUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {

    static private TwitchBot twitchBot;
    static private GoogleSheets googleSheets;
    static private final RecognizingProtocol recognizingProtocol = new RecognizingProtocol();
    static private final M3U8Controller m3U8Controller = new M3U8Controller();
    static private File logsFile;
    static private Map<String, List<String>> lastEvents;

    static private List<String> nicks;
    static private final Map<String, String> gamesLength = new HashMap<>();

    static private final ChatBot chatBot = new ChatBot();
    static private final ComicBot comicBot = new ComicBot();
    static private final SimpleApi httpClient = new SimpleApi();
    static private final EmotesController emotesController = new EmotesController();
    static private final CommandExecutor commandExecutor = new CommandExecutor();
    static private final long lastUpdate = System.currentTimeMillis();
    static private final int updateMinutes = 30;
    static private long lastEventCheck = 0;
    static public boolean isStopped = false;
    static public boolean isCheckedEvents = false;

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

    static public void handleMessage(String channelname, String username, Set userPermissions, String message) {
        if (CommandConstants.masterNames.contains(username)) {
            userPermissions.add("MASTER");
        }
        userPermissions.add(username.toUpperCase());
        for (var s: CommandConstants.blacklist
        ) {
            if (message.contains(s)) {
                return;
            }
        }

        handleNick(username);

        if (message.startsWith("!") && !CommandConstants.botNames.contains(username)) {
            String commandTag = message.replaceFirst("(!\\S+).*", "$1");
            String commandArgs = message.replaceAll("!\\S+\\s?(.*)","$1");
            if (commandExecutor.containsCommand(commandTag)) {
                String result = commandExecutor.execute(commandTag, channelname, username, userPermissions, commandArgs);
                if (result != null) {
                    twitchBot.sendMessage(result, channelname, false);
                }
            }
        }

        if (isStopped) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            twitchBot.close();
            return;
        }

        long currEventCheck = System.currentTimeMillis();
        String nick = CommandConstants.nicknames.get(channelname);
        //if (nick == null) nick = "UselessMouth";
        if (isCheckedEvents) {
            if ((currEventCheck - lastEventCheck) >= 60*1000 && nick != null) {
                lastEventCheck = System.currentTimeMillis();
                String lastEventMessage = MainController.updateLastEvent(nick);
                if (lastEventMessage != null) {
                    twitchBot.sendMessage(lastEventMessage, channelname);
                }

            }
        }

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
            String game = infoMap.get("Game");
            String length = "???";

            if (gamesLength.containsKey(game)) {
                length = gamesLength.get(game);
            } else {
                length = httpClient.getGameTimeFromHLTB(infoMap.get("Game"));
                if (length.equals("???") || length.equals("--")) {
                    length = httpClient.getTimeFromGamefaqs(game);
                }
                if (!length.equals("???")) {
                    gamesLength.put(game, length);
                }

            }
            sbInfo.append("Время прохождения: [" + length + "], ");
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

            pattern = Pattern.compile("\\+\\d+[^%\\d]");
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



    static public String getArt(String emote, String channel, int threshold) {
        long time = System.currentTimeMillis();
        long diff = time - lastUpdate;
        if (diff/(1000*60) >= updateMinutes) {
            emotesController.updateChannelEmotes(channel);
        }
        String url = emotesController.getEmoteUrl(emote, channel);
        if (url == null) {
            return null;
        }

        if (threshold > 100 || threshold < 0) {
            threshold = -1;
        }
        String art = null;

        try {
            BufferedImage image = ImageController.getImageFromUrl(url);
            art = ImageController.ImageToBraille(image, threshold);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return art;
    }

    static public void main (String[] args){
        Scanner sc = new Scanner(System.in);
        String channel = sc.nextLine();
        System.out.println(getShazam(channel));
    }

    static public String getShazam(String channel) {
        String result = "Произошла хуйня :)";
        List<String> urls = m3U8Controller.getLastTsUrls(channel, 4);
        if (urls == null) {
            return "Не удалось получить поток";
        }
        List<File> filesFromUrls = FFMpegUtil.urlsToTSfiles(urls);
        File mp3 = FFMpegUtil.encodeTStoMP3(filesFromUrls);
        try {
            byte[] data = new FileInputStream(mp3).readAllBytes();
            mp3.delete();
            Map<String, String> resultMap = recognizingProtocol.recognize(data, "audio");
            if (resultMap.containsKey("result")) {
                result = resultMap.get("result");
                result = "Совпадения: " + result;
            } else {
                result = resultMap.get("status");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        return result;
    }

    static public void addListeningChannel(String channel) {
        m3U8Controller.addChannel(channel);
    }

    static public void handleNick(String nick) {
        if (nicks == null) {
            nicks = new ArrayList<>();
        }

        if (!nicks.contains(nick)) {
            nicks.add(nick);
        }
    }

    static public void sendMessage (String message, String channelName) {
        twitchBot.sendMessage(message, channelName);
    }





    static public String getAnswerFromChatbot(String msg){
        String answer = chatBot.getAnswer(msg);
        return answer;
    }

    static public String getAnswerFromComicbot() {
        String answer = comicBot.getAnek();
        return answer;
    }

    static private final String[] whenAnswers = {"сейчас", "завтра", "вчера", "через полчаса",
            "никогда SadCat", "всегда widepeepoHappy", "через неделю", "через год", "когда юзя видеокарту разгонит",
            "а хуй знает"};
    static public String when() {
        Random rd = new Random();
        String answer = whenAnswers[rd.nextInt(whenAnswers.length)];
        return answer;
    }

    static private final String[] whoAnswers = {"он", "ты", "я", "никто",
            "все", "кто-то", "чат"};

    static public String who() {
        Random rd = new Random();
        StringBuilder answ = new StringBuilder(whoAnswers[rd.nextInt(whoAnswers.length)]);
        if (answ.toString().equals("он")) {
            answ.append(" :point_right: @" + nicks.get(rd.nextInt(nicks.size())));
        }

        return answ.toString();
    }

    static private final String[] whereAnswers = {"там :point_right:", "там :point_up:",
            "там :point_down:", "там :point_left:", "сзади monkaW", "за окном", "нигде",
    "хз, поищи"};
    static public String where() {
        Random rd = new Random();
        String answ = whereAnswers[rd.nextInt(whereAnswers.length)];
        return answ;
    }

    public static String joinTo(String channel) {
        emotesController.updateChannelEmotes(channel);
        return twitchBot.joinToChannel(channel);
    }

    public static void setMaxPastCount(int count) {
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

    public static void sendPMmessage(String username, String message) {
        twitchBot.sendMessagePm(message, username);
    }

    public static String getAnek() {
        String answer = null;
        int i = 0;
        while (true) {
            boolean isBL = false;
            answer = httpClient.getAnek();
            for (var bl: CommandConstants.blacklist
            ) {
                if (answer.contains(bl)) {
                    isBL = true;
                    break;
                }
            }
            if ((answer.length() <= 500 || i > 15) && !isBL){
                break;
            }
            i++;
        }

        return answer;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        twitchBot.close();
    }
}
