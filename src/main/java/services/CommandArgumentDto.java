package services;


import java.util.Collections;
import java.util.Set;

public class CommandArgumentDto {

    private String channelname;
    private String username;
    private Set userPermissions;
    private String message;

    public CommandArgumentDto(String channelname, String username, Set userPermissions, String message) {
        this.channelname = channelname;
        this.username = username;
        this.userPermissions = userPermissions;
        this.message = message;
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
}


