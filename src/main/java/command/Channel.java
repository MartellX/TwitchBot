package command;

import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO собственно сделать
public class Channel {
    private CommandConfigService commandConfigService;
    private CommandExecutor executor;
    private String name;

    public Channel(String name) {
        this.name = name;
    }

    public Channel(CommandConfigService commandConfigService, String name) {
        this.commandConfigService = commandConfigService;
        this.name = name;
    }

    public Channel(String name, Map<String, Command> commands) {
        this(name);
        executor = new CommandExecutor(commandConfigService ,commands);
    }

    public class Builder{

    }

    public void execute(String commandTag,
                        String channelname,
                        String username,
                        Set<String> userPermissions,
                        String message) {
        executor.execute(commandTag, channelname, username, userPermissions, message);
    }
}


