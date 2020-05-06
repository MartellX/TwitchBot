import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.api.domain.IDisposable;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

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

    TwitchBot(OAuth2Credential credential) {
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
                "unclebjorn", "бьерн", "бьёрн", "бьорн", "бурн"
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

        /*
        long timeReconnect = System.currentTimeMillis();

        if((timeReconnect - lastReconnect) > 1000*60*10) {
            twitchClient.getChat().reconnect();
        }

         */


        String logMessage = ("[" + new Date() + "][" + event.getChannel().getName() + "]["
                     + event.getPermissions().toString()+"] "
                     + event.getUser().getName() + ": "
                     + event.getMessage());
        //MainController.writeToLogs(logMessage);
        System.out.println(logMessage);
        long sendMessageTime = System.currentTimeMillis();
        if ((sendMessageTime - lastSendedMessage) < (10*1000)) {
             return;
         }

        if (botNames.contains(event.getUser().getName())) {
            return;
        }

        MainController.handleNick(event.getUser().getName());

         String channelName = event.getChannel().getName().toLowerCase();
         String nick = nicknames.get(channelName);
         //String nick = "uselessmouth";
         String message = event.getMessage();
         if (message.startsWith("!hpg_top") || message.startsWith("!хпгтоп")) {
             StringBuilder sb = new StringBuilder();
             sb.append("Топ: ");
             sb.append(MainController.getTop());
             sb.append(" @" + event.getUser().getName());
             sendMessage(event.getChannel().getName(), sb.toString());

         } else if (message.startsWith("!hpg_info") || message.startsWith("!хпгинфо")) {
             String nickInfo = nick;
             if (message.matches("\\S+ \\S+")) {
                 nickInfo = message.split(" ")[1];
                 nickInfo = getNick(nickInfo);
                 if (nickInfo == null) {
                     String msg = "Не понял. Ху? @" + event.getUser().getName();
                     sendMessage(event.getChannel().getName(), msg);
                     return;
                 }
             }
             StringBuilder sb = new StringBuilder(nickInfo + ": ");
             sb.append(MainController.getInfoAbout(nickInfo));
             sb.append(" @" + event.getUser().getName());
             sendMessage(event.getChannel().getName(), sb.toString());

         } else if (message.startsWith("!паста")) {
             sendMessage(event.getChannel().getName(), MainController.getPast());

         } else if (message.startsWith("!помощь")) {
             String commandMessage = "Это тестовый бот для слежения за процессом HPG. (Задержка 10 секунд)" +
                     "Доступные команды: !хпгтоп, !хпгинфо, !хпгинфо [ник], !паста, " +
                     "!кто, !где, !когда, !событие,  !анфиса [сообщение], " +
                     "!анек. Для того, чтобы предложить команду используйте !предложить [сообщение]";
             sendMessage(event.getChannel().getName(), commandMessage);

         } else if (message.startsWith("!когда")) {
             sendMessage(event.getChannel().getName(), MainController.when());

         } else if(message.startsWith("!кто")) {
             String who = MainController.who();
             if (who.equals("ты")) {
                 who = (who + " @" + event.getUser().getName());
             }
             sendMessage(event.getChannel().getName(), who);

         } else if (message.startsWith("!где")) {
             String where = MainController.where();
             sendMessage(event.getChannel().getName(), where);

         } else if (message.startsWith("!событие")) {
             String last = MainController.getLastEvent(nick);
             if (last != null) {
                 sendMessage(event.getChannel().getName(), last + " @" + event.getUser().getName());
             }

         } else if (message.startsWith("!анфиса")) {
             String msg = message.replaceFirst("!анфиса", "");
             if (!msg.matches("\\s*?")) {
                 String answer = MainController.getAnswerFromChatbot(msg) + " @" + event.getUser().getName();
                 sendMessage(event.getChannel().getName(), answer);
             }

         } else if (message.startsWith("!анек")) {
             String answer = "";
             int i = 0;
             while (true) {
                 answer = MainController.getAnswerFromComicbot();
                 if (answer.length() <= 500 || i > 15) {
                     break;
                 }
                 i++;
             }

             sendMessage(event.getChannel().getName(), answer);

         } else if (message.startsWith("!предложить")) {
             String msg = message.replaceFirst("!предложение", "");
             if (!msg.matches("\\s*?")) {
                 System.out.println("[SUGGESTION][" + new Date() + "][" + event.getUser().getName() + "]: "
                         + msg);
                 String answer = "Предложение отправлено!";
                 sendMessage(event.getChannel().getName(), answer);
             }

         } else if(message.startsWith("!martell_stop") && event.getUser().getName().equals("martellx")) {
             sendMessage(event.getChannel().getName(), "останавливаюсь... peepoRIP");
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             twitchClient.getEventManager().close();
             twitchClient.getClientHelper().close();
             twitchClient.close();

         } else if(message.startsWith("!jointo") && event.getUser().getName().equals("martellx")) {

             if (message.matches("\\S+ \\S+")) {
                 String joinTo = message.split(" ")[1];
                 joinTo = getNick(joinTo);
                 if (joinTo != null) {
                     joinToChannel(joinTo);
                 }
             }

         } else if(message.startsWith("!setpastcount") && event.getUser().getName().equals("martellx")) {

             if (message.matches("\\S+ \\d+")) {
                 String count = message.split(" ")[1];
                 MainController.setMaxPastCount(Integer.parseInt(count));
             }

         }
         else if (!message.startsWith("!")){
             String response = MainController.handleMessage(message);
             if (response != null) {
                 sendMessage(event.getChannel().getName(), response, false);
             }

         }

         long timeCheck = System.currentTimeMillis();
         if ((timeCheck - lastTimeCheck) >= 10*1000 && nick != null) {
                 String lastEventMessage = MainController.updateLastEvent(nick);
                 if (lastEventMessage != null) {
                     sendMessage(event.getChannel().getName(), lastEventMessage, false);
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
        lastSendedMessage = System.currentTimeMillis();
        System.out.println("[LOGS][" + new Date() + "][SEND_MESSAGE] " + message);
        twitchClient.getChat().sendMessage(channelName, "/me " + message);
    }

    void sendMessage (String channelName, String message, boolean isDelaying) {

        /*String sendingMessage = null;
        try {
            sendingMessage = new String(message.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

         */
        if (isDelaying) {
            lastSendedMessage = System.currentTimeMillis();
        }
        System.out.println("[LOGS][" + new Date() + "][SEND_MESSAGE] " + message);
        twitchClient.getChat().sendMessage(channelName, "/me " + message);
    }

    void joinToChannel(String channel) {
        twitchClient.getChat().joinChannel(channel);
        sendMessage("martellx", ("Присоединился к каналу: \"" + channel + "\""), false);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        twitchClient.close();
    }
}

