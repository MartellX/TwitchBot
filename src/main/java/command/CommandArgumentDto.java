package command;


import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

import java.util.Set;

public class CommandArgumentDto {

    private final String channelname;
    private final String username;
    private final Set userPermissions;
    private final String message;
    private final String emotesInfo;
    private final IRCMessageEvent messageEvent;

    public CommandArgumentDto(String channelname, String username, Set userPermissions, String message, String emotesInfo, IRCMessageEvent messageEvent) {
        this.channelname = channelname;
        this.username = username;
        this.userPermissions = userPermissions;
        this.message = message;
        this.emotesInfo = emotesInfo;
        this.messageEvent = messageEvent;
    }

    public String getChannelname() {
        return channelname;
    }

    public String getUsername() {
        return username;
    }

    public Set getUserPermissions() {
        return userPermissions;
    }

    public String getMessage() {
        return message;
    }

    public IRCMessageEvent getMessageEvent() {
        return messageEvent;
    }

    public String getEmotesInfo() {
        return emotesInfo;
    }
}


