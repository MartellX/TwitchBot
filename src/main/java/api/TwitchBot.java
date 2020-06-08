package api;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.api.domain.IDisposable;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;
import com.github.twitch4j.common.events.channel.ChannelGoLiveEvent;
import com.github.twitch4j.common.events.channel.ChannelGoOfflineEvent;
import controllers.MainController;

import java.beans.EventHandler;
import java.util.*;

public class TwitchBot {
    TwitchClient twitchClient;
    IDisposable handlerMessages;
    boolean isClosed = false;


    public TwitchBot(OAuth2Credential credential) {

        this.twitchClient = TwitchClientBuilder.builder()
                .withEnableChat(true)
                .withChatAccount(credential)
                .withClientId("***REMOVED***")
                .withEnableHelix(true)
                .withDefaultAuthToken(credential)
                .build();
        //twitchClient.getClientHelper().setDefaultAuthToken(credential);
        SimpleEventHandler eventHandler = twitchClient.getEventManager()
                .getEventHandler(SimpleEventHandler.class);

        eventHandler.onEvent(ChannelMessageEvent.class, event -> handlerMethod(event));


        eventHandler.onEvent(ChannelGoLiveEvent.class, event -> {
            MainController.handleMessage(event.getChannel().getName(), "", new HashSet(Set.of("MASTER")), "!выкл фан");
        });

        eventHandler.onEvent(ChannelGoOfflineEvent.class, event -> {
            MainController.handleMessage(event.getChannel().getName(), "", new HashSet(Set.of("MASTER")), "!вкл фан");
        });

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
        twitchClient.getClientHelper().enableStreamEventListener(channel);
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

