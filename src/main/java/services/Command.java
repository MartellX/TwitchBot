package services;

import java.util.function.Function;

public class Command {
    private Function<CommandArgumentDto, String> command;
    private CommandConfig config;
    private long lastExecution;
    private CommandType type;

    public Command(Function<CommandArgumentDto, String> command, CommandType type) {
        this.command = command;
        this.type = type;
        this.config = type.getConfigClone();
        this.lastExecution = 0;
    }

    public Command(Function<CommandArgumentDto, String> command, CommandConfig config) {
        this.command = command;
        this.type = CommandType.OTHER;
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

    public CommandType getType() {
        return type;
    }
}
