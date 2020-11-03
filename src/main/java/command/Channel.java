package command;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO собственно сделать
public class Channel {
    private CommandConfigService commandConfigService;
    private CommandExecutor executor;
    private String name;
    Map<String, CommandConfig> commands;

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

    private Channel(){
        name = "Unnamed";
        commandConfigService = CommandConfigService.getDefault();
        commands = new HashMap<>();
    }

    public static class Builder{
        private Channel channel;

        public Builder(){
            channel = new Channel();
        }

        public Builder setName(String name) {
            channel.name = name;
            return this;
        }

        public Builder setConfigService(CommandConfigService service) {
            channel.commandConfigService = service;
            return this;
        }

        public Builder addCommands(Map<String, CommandConfig> commands) {
            channel.commands.putAll(commands);
            return this;
        }



        public Channel build(){
            channel.executor = new CommandExecutor();
            channel.executor.setCommandConfigService(channel.commandConfigService);
            for (var command: channel.commands.entrySet()
                 ) {
                String alias = command.getKey();
                CommandConfig config = command.getValue();
                channel.executor.getCommand(alias).setConfig(config);
            }

            return channel;
        }

    }

    public void execute(String commandTag,
                        String channelname,
                        String username,
                        Set<String> userPermissions,
                        String message,
                        IRCMessageEvent messageEvent) {

        executor.execute(commandTag, channelname, username, userPermissions, message, messageEvent);
    }
}


