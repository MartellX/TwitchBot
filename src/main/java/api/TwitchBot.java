package api;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.api.domain.IDisposable;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import controllers.MainController;

import java.util.*;

public class TwitchBot {
    TwitchClient twitchClient;
    IDisposable handlerMessages;
    boolean isClosed = false;

    long lastTimeCheck;
    long lastReconnect = 0;
    long lastSendedMessage = 0;
    Map<String, String> nicknames = new HashMap<>();
    Map<String, List<String>> nicknameVariables = new HashMap<>();
    Set<String> botNames = new HashSet<>();
    Set<String> blacklist = new HashSet<>();
    Set<String> adminNames = new HashSet<>();
    int delay = 10;

    public TwitchBot(OAuth2Credential credential) {
        lastTimeCheck = System.currentTimeMillis() - 1000*60;


        nicknames.put("uselessmouth", "UselessMouth");
        nicknameVariables.put("uselessmouth", Arrays.asList(
                "юзя", "uselessmouth", "гений", "ричард", "разгонщик"
                ));

        nicknames.put("mistafaker", "Mistafaker");
        nicknameVariables.put("mistafaker", Arrays.asList(
                "факер", "faker", "fucker", "фхс", "mistafaker", "фейкер", "гном"
        ));

        nicknames.put("melharucos", "Melharucos");
        nicknameVariables.put("melharucos", Arrays.asList(
                "мэл", "мел", "melharucos", "mel", "мельха"
        ));

        nicknames.put("unclebjorn", "UncleBjorn");
        nicknameVariables.put("unclebjorn", Arrays.asList(
                "unclebjorn", "бьерн", "бьёрн", "бьорн", "бурн", "мишка", "медведь", "миха", "михаил"
        ));

        nicknames.put("liz0n", "liz0n");
        nicknameVariables.put("liz0n", Arrays.asList(
                "лиза", "лизон", "пиздон", "liz0n", "elizzavetta", "lison", "lizon"
        ));

        nicknames.put("lasqa", "Lasqa");
        nicknameVariables.put("lasqa", Arrays.asList(
                "ласка", "крыса", "lasqa", "бодя"
        ));

        botNames.add("nightbot");
        botNames.add("moobot");
        botNames.add("streamlabs");
        botNames.add("hepega_bot");

        blacklist.add("педик");
        blacklist.add("пидорас");
        blacklist.add("пидор");
        blacklist.add("пидрила");
        blacklist.add("пидр");
        blacklist.add("п***р");
        blacklist.add("п****");
        blacklist.add("нигга");
        blacklist.add("нигер");
        blacklist.add("ниггер");
        blacklist.add("негр");
        blacklist.add("неггр");



        adminNames.add("martellx");
        adminNames.add("pdvrr");


        this.twitchClient = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .withChatAccount(credential)
                .withEnableHelix(true)
                .build();
        handlerMessages = twitchClient
                .getEventManager()
                .getEventHandler(SimpleEventHandler.class)
                .onEvent(ChannelMessageEvent.class, event -> handlerMethod(event));

    }

    boolean isStarted = false;

     void handlerMethod(ChannelMessageEvent event) {

        if(isClosed) {
            close();
            return;
        }

        /*
        long timeReconnect = System.currentTimeMillis();

        if((timeReconnect - lastReconnect) > 1000*60*10) {
            twitchClient.getChat().reconnect();
        }

         */
         Set<String> permissions = new HashSet<>();
         event.getPermissions().forEach(t -> permissions.add(t.toString()));
         String channelname = event.getChannel().getName();

         String username = event.getUser().getName();
         String message = event.getMessage();


        String logMessage = ("[" + new Date() + "][" + channelname + "]["
                     + permissions.toString()+"] "
                     + username + ": "
                     + message);

        //controllers.MainController.writeToLogs(logMessage);
        System.out.println(logMessage);

        MainController.handleMessage(channelname, username, permissions, message);





     }

     String getNick(String nickVar) {
        for(Map.Entry<String, List<String>> e : nicknameVariables.entrySet()) {
            if (e.getValue().contains(nickVar)) {
                return e.getKey();
            }
        }

        return null;
     }

     public void close() {
         isClosed = true;
         twitchClient.getClientHelper().close();
         twitchClient.close();
     }

    public void sendMessage (String message, String channelName) {
        System.out.println("[LOGS][" + new Date() + "][SEND_MESSAGE] " + message);
        twitchClient.getChat().sendMessage(channelName, "/me " + message);
    }


    public String joinToChannel(String channel) {
        twitchClient.getChat().joinChannel(channel);
        String message = "Присоединился к каналу: \"" + channel + "\"";
        sendMessage(message, "martellx");
        return message;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        twitchClient.close();
    }
}

