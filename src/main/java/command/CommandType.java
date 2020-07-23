package command;

import java.util.Map;

public enum CommandType {

    INFO{

        public CommandConfig getConfigClone(CommandConfigService commandConfigService) {
            return commandConfigService.getInfoConfigClone();
        }

        @Override
        public CommandConfig getConfig(CommandConfigService commandConfigService) {
            return commandConfigService.getInfoConfig();
        }
    },
    FUN{

        public CommandConfig getConfigClone(CommandConfigService commandConfigService) {
            return commandConfigService.getFunConfigClone();
        }

        @Override
        public CommandConfig getConfig(CommandConfigService commandConfigService) {
            return commandConfigService.getFunConfig();
        }
    },
    MOD{

        public CommandConfig getConfigClone(CommandConfigService commandConfigService) {
            return commandConfigService.getModConfigClone();
        }

        @Override
        public CommandConfig getConfig(CommandConfigService commandConfigService) {
            return commandConfigService.getModConfig();
        }
    },
    MASTER{
        public CommandConfig getConfigClone(CommandConfigService commandConfigService) {
            return commandConfigService.getMasterConfigClone();
        }

        @Override
        public CommandConfig getConfig(CommandConfigService commandConfigService) {
            return commandConfigService.getMasterConfig();
        }
    },
    OTHER {




        @Override
        public CommandConfig getConfigClone(CommandConfigService commandConfigService) {
            return commandConfigService.getInfoConfigClone();
        }

        @Override
        public CommandConfig getConfig(CommandConfigService commandConfigService) {
            return commandConfigService.getInfoConfig();
        }


    };





    private static Map<String, CommandType> typesMap = Map.of(
            "INFO", CommandType.INFO,
            "FUN", CommandType.FUN,
            "MOD", CommandType.MOD,
            "MASTER", CommandType.MASTER,
            "OTHER", CommandType.OTHER);
    public static CommandType getTypeFromString(String type) {
        return typesMap.get(type);
    }
    public abstract CommandConfig getConfigClone(CommandConfigService commandConfigService);
    public abstract CommandConfig getConfig(CommandConfigService commandConfigService);

}
