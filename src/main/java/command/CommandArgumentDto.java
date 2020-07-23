package command;


import java.util.Set;

public class CommandArgumentDto {

    private final String channelname;
    private final String username;
    private final Set userPermissions;
    private final String message;

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


