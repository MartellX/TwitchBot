package command;

import java.util.Set;

//TODO собственно сделать
public class Channel {
    private CommandConfigService commandConfigService;
    private CommandExecutor executor;


    public void execute(String commandTag,
                        String channelname,
                        String username,
                        Set<String> userPermissions,
                        String message) {
        executor.execute(commandTag, channelname, username, userPermissions, message);
    }
}
