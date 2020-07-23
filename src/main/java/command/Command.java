package command;

import java.util.function.Function;

public class Command {
    private Function<CommandArgumentDto, String> command;
    private CommandConfig config;
    private long lastExecution;


    public Command(Function<CommandArgumentDto, String> command, CommandType type) {
        this.command = command;
        this.config = type.getConfigClone(CommandConfigService.getDefault()); //TODO пофиксить получение конфига
        this.lastExecution = 0;
    }

    public Command(Function<CommandArgumentDto, String> command, CommandConfig config) {
        this.command = command;
        this.config = config;
        this.lastExecution = 0;
    }

    public Function<CommandArgumentDto, String> getCommand() {
        return command;
    }

    public void setCommand(Function<CommandArgumentDto, String> command) {
        this.command = command;
    }

    public CommandConfig getConfig() {
        return config;
    }

    public void setConfig(CommandConfig config) {
        this.config = config;
    }

    public long getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(long lastExecution) {
        this.lastExecution = lastExecution;
    }

}
