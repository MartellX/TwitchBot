package services;

import java.util.Set;

public class CommandConfigService {

    private CommandConfig infoConfig, funConfig, modConfig, masterConfig;

    public CommandConfigService() {
        infoConfig = new CommandConfig(3, Set.of("EVERYONE"));
        funConfig = new CommandConfig(30, Set.of("EVERYONE"), true);
        modConfig = new CommandConfig(0, Set.of("MODERATOR", "MASTER"));
        masterConfig = new CommandConfig(0, Set.of("MASTER"));
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
