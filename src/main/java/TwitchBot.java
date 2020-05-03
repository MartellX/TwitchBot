import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.api.domain.IDisposable;
import com.github.philippheuer.events4j.core.domain.Event;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import java.io.UnsupportedEncodingException;
import java.util.*;

public class TwitchBot {
    TwitchClient twitchClient;
    IDisposable handlerMessages;
    boolean isClosed = false;

    long lastTimeCheck;
    Map<String, String> nicknames = new HashMap<>();
    Map<String, List<String>> nicknameVariables = new HashMap<>();

    TwitchBot(OAuth2Credential credential) {
        lastTimeCheck = System.currentTimeMillis() - 1000*60;

        nicknames.put("uselessmouth", "UselessMouth");
        nicknameVariables.put("uselessmouth", Arrays.asList(
                "юзя", "uselessmouth", "гений", "ричард"
                ));

        nicknames.put("mistafaker", "Mistafaker");
        nicknameVariables.put("mistafaker", Arrays.asList(
                "факер", "faker", "fucker", "фхс", "mistafaker"
        ));

        nicknames.put("melharucos", "Melharucos");
        nicknameVariables.put("melharucos", Arrays.asList(
                "мэл", "мел", "melharucos", "mel"
        ));

        nicknames.put("unclebjorn", "UncleBjorn");
        nicknameVariables.put("unclebjorn", Arrays.asList(
                "unclebjorn", "бьерн", "бьёрн"
        ));

        nicknames.put("liz0n", "liz0n");
        nicknameVariables.put("liz0n", Arrays.asList(
                "лиза", "лизон", "liz0n"
        ));

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

     void handlerMethod(ChannelMessageEvent event) {
        if(isClosed) {
            twitchClient.getClientHelper().close();
            twitchClient.close();
            return;
        }
             String logMessage = new String(("[" + new Date() + "][" + event.getChannel().getName() + "]["
                     + event.getPermissions().toString()+"] "
                     + event.getUser().getName() + ": "
                     + event.getMessage())
             );

             MainController.writeToLogs(logMessage);
             System.out.println(logMessage);

         String channelName = event.getChannel().getName().toLowerCase();
         //String nick = nicknames.get(channelName);
         String nick = "uselessmouth";
         String message = event.getMessage();
         if (message.startsWith("!hpg_top")) {
                 StringBuilder sb = new StringBuilder();
                 sb.append("Топ 5: ");
                 sb.append(MainController.getTop());
                 sb.append(" @" + event.getUser().getName());
                 sendMessage(event.getChannel().getName(), sb.toString());
         } else if (message.startsWith("!hpg_info")) {
                 String nickInfo = nick;
                 if (message.matches("\\S+ \\S+")) {
                     nickInfo = message.split(" ")[1];
                     nickInfo = getNick(nickInfo);
                     if (nickInfo == null) {
                         nickInfo = nick;
                     }
                 }
                 if(nickInfo == null) return;
                 StringBuilder sb = new StringBuilder(nickInfo + ": ");
                 sb.append(MainController.getInfoAbout(nickInfo));
                 sb.append(" @" + event.getUser().getName());
                 sendMessage(event.getChannel().getName(), sb.toString());
             }

             long timeCheck = System.currentTimeMillis();
             if ((timeCheck - lastTimeCheck) >= 30*1000) {
                 String lastEventMessage = MainController.getLastEvent(nick);
                 if (lastEventMessage != null) {
                     sendMessage(event.getChannel().getName(), lastEventMessage);
                 }
                 lastTimeCheck = System.currentTimeMillis();
             }

     }

     String getNick(String nickVar) {
        for(Map.Entry<String, List<String>> e : nicknameVariables.entrySet()) {
            if (e.getValue().contains(nickVar)) {
                return e.getKey();
            }
        }

        return null;
     }

    void sendMessage (String channelName, String message) {

        /*String sendingMessage = null;
        try {
            sendingMessage = new String(message.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

         */
        twitchClient.getChat().sendMessage(channelName, message);
    }

    void joinToChannel(String channel) {
        twitchClient.getChat().joinChannel(channel);
        sendMessage(channel, ("Присоединился к каналу: \"" + channel + "\""));
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        twitchClient.close();
    }
}

