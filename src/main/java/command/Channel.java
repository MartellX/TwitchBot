package command;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO собственно сделать
public class Channel {

    private String name;
    private int id;
    private boolean isLive;
    Map<String, CommandConfig> commands;

    private CommandConfigService commandConfigService;
    private CommandExecutor executor;

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

    public Map<String, CommandConfig> getCommands() {
        return commands;
    }

    public void setCommands(Map<String, CommandConfig> commands) {
        this.commands = commands;
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

        public Builder setID (int id) {
            channel.id = id;
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

            channel.commands.clear();
            for (CommandConfig config:channel.executor.getConfigs()
                 ) {
                channel.commands.put(config.getName(), config);
            }

            return channel;
        }

    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }


}


