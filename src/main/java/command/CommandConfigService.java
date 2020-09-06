package command;

import java.util.HashSet;
import java.util.Set;

public class CommandConfigService {

    private static final CommandConfigService defaultConfigService = new CommandConfigService();

    private CommandConfig infoConfig, funConfig, modConfig, masterConfig;

    public CommandConfigService() {
        infoConfig = new CommandConfig(5, new HashSet(Set.of("EVERYONE")), CommandType.INFO);
        funConfig = new CommandConfig(60, new HashSet(Set.of("EVERYONE")), CommandType.FUN);
        modConfig = new CommandConfig(0, new HashSet(Set.of("MODERATOR", "MASTER")), CommandType.MOD);
        masterConfig = new CommandConfig(0, new HashSet(Set.of("MASTER")), CommandType.MASTER);
    }

    public static CommandConfigService getDefault(){
        return defaultConfigService;
    }

    public CommandConfig getInfoConfig() {
        return infoConfig;
    }

    public CommandConfig getFunConfig() {
        return funConfig;
    }

    public CommandConfig getModConfig() {
        return modConfig;
    }

    public CommandConfig getMasterConfig() {
        return masterConfig;
    }

    public CommandConfig getInfoConfigClone() {
        return infoConfig.clone();
    }

    public CommandConfig getConfig(CommandType type){
        return type.getConfig(this);
    }

    public CommandConfig getConfigClone(CommandType type){
        return type.getConfigClone(this);
    }

    public void setInfoConfig(CommandConfig infoConfig) {
        this.infoConfig = infoConfig;
    }

    public CommandConfig getFunConfigClone() {
        return funConfig.clone();
    }

    public void setFunConfig(CommandConfig funConfig) {
        this.funConfig = funConfig;
    }

    public CommandConfig getModConfigClone() {
        return modConfig.clone();
    }

    public void setModConfig(CommandConfig modConfig) {
        this.modConfig = modConfig;
    }

    public CommandConfig getMasterConfigClone() {
        return masterConfig.clone();
    }

    public void setMasterConfig(CommandConfig masterConfig) {
        this.masterConfig = masterConfig;
    }

    public CommandConfig getCustomConfig(int delay, Set permissions, boolean isPaused) {
        return new CommandConfig(delay, permissions, isPaused);
    }
}
