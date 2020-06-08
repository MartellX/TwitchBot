package services;

public enum CommandType {
    INFO{
        public CommandConfig getConfigClone() {
            return commandConfigService.getInfoConfigClone();
        }

        @Override
        public CommandConfig getConfig() {
            return commandConfigService.getInfoConfig();
        }
    },
    FUN{
        public CommandConfig getConfigClone() {
            return commandConfigService.getFunConfigClone();
        }

        @Override
        public CommandConfig getConfig() {
            return commandConfigService.getFunConfig();
        }
    },
    MOD{
        public CommandConfig getConfigClone() {
            return commandConfigService.getModConfigClone();
        }

        @Override
        public CommandConfig getConfig() {
            return commandConfigService.getModConfig();
        }
    },
    MASTER{
        public CommandConfig getConfigClone() {
            return commandConfigService.getMasterConfigClone();
        }

        @Override
        public CommandConfig getConfig() {
            return commandConfigService.getMasterConfig();
        }
    },
    OTHER {
        @Override
        public CommandConfig getConfigClone() {
            return commandConfigService.getInfoConfigClone();
        }

        @Override
        public CommandConfig getConfig() {
            return commandConfigService.getInfoConfig();
        }
    };


    public abstract CommandConfig getConfigClone();
    public abstract CommandConfig getConfig();
    private static CommandConfigService commandConfigService;

    public static void setCommandConfigService(CommandConfigService commandConfigService) {
        CommandType.commandConfigService = commandConfigService;
    }
}
