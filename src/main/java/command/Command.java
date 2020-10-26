package command;

import java.lang.reflect.Method;
import java.util.function.Function;

public class Command {
    private String name;
    private Function<CommandArgumentDto, String> command;
    private Method method;
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

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Command clone() {
        CommandConfig config = this.config.clone();
        Command command = new Command(this.command, config);
        command.setMethod(method);
        command.setName(name);
        return command;
    }
}
