package command;

import constants.CommandConstants;
import controllers.MainController;

import java.util.*;
import java.util.function.Function;

public class CommandExecutor {
    private Map<String, Command> commands = new HashMap<>();
    private final CommandConfigService commandConfigService;

    public String execute(String commandTag, String channelname, String username, Set<String> userPermissions, String message){
        Command command = commands.get(commandTag);
        Function<CommandArgumentDto, String> commandFunction = command.getCommand();
        Set neededPermissions = command.getConfig().getNeededPermissions();
        int neededDelay = command.getConfig().getDelay();
        long lastExecute = command.getLastExecution();
        long currentTime = System.currentTimeMillis();
        CommandArgumentDto args = new CommandArgumentDto(channelname, username, userPermissions, message);
        String result;
        if (Collections.disjoint(userPermissions, neededPermissions)) {
            result = null;
        } else if ((currentTime - lastExecute) < neededDelay * 1000 || command.getConfig().isPaused()) {
            result = null;
        } else {

            result = commandFunction.apply(args);
            if (result != null) {
                command.setLastExecution(System.currentTimeMillis());
                if (command.getConfig().getType() != CommandType.FUN) {
                    result = "/me " + result;
                }
            }
        }

        return result;
    }

    public CommandExecutor() {
        commandConfigService = new CommandConfigService();
        Command tecCommand;
        //----------------------------------------------------------------------------------------------
//        tecCommand = new Command(this::getHpgTop, CommandType.INFO);
//        commands.put("!хпгтоп", tecCommand);
//        commands.put("!hpgtop", tecCommand);
//
//        tecCommand = new Command(this::getHpgInfo, CommandType.INFO);
//        commands.put("!хпгинфо", tecCommand);
//        commands.put("!hpginfo", tecCommand);
//
//        tecCommand = new Command(this::getHelp, CommandType.INFO);
//        commands.put("!помощь", tecCommand);
//
//        tecCommand = new Command(this::getEvent, CommandType.INFO);
//        commands.put("!событие", tecCommand);

        //----------------------------------------------------------------------------------------------

        tecCommand = new Command(this::getPaste, CommandType.FUN);
        commands.put("!паста", tecCommand);

        tecCommand = new Command(this::chatBotAnswer, CommandType.FUN);
        commands.put("!анфиса", tecCommand);

        tecCommand = new Command(this::getComic, CommandType.FUN);
        //commands.put("!анек", tecCommand);

        tecCommand = new Command(this::getArt, CommandType.FUN);
        commands.put("!арт", tecCommand);

        tecCommand = new Command(this::who, CommandType.FUN);
        commands.put("!кто", tecCommand);

        tecCommand = new Command(this::where, CommandType.FUN);
        commands.put("!где", tecCommand);

        tecCommand = new Command(this::when, CommandType.FUN);
        commands.put("!когда", tecCommand);

        tecCommand = new Command(this::getArt, CommandType.FUN);
        commands.put("!арт", tecCommand);

        tecCommand = new Command(this::roulette, CommandType.FUN);
        commands.put("!рулетка", tecCommand);

        //----------------------------------------------------------------------------------------------

        tecCommand = new Command(this::setDelay, CommandType.MOD);
        commands.put("!задержка", tecCommand);

        tecCommand = new Command(this::offCommand, CommandType.MOD);
        commands.put("!выкл", tecCommand);

        tecCommand = new Command(this::onCommand, CommandType.MOD);
        commands.put("!вкл", tecCommand);


        //----------------------------------------------------------------------------------------------

        tecCommand = new Command(this::joinToChannel, CommandType.MASTER);
        commands.put("!jointo", tecCommand);

        tecCommand = new Command(this::pauseBot, CommandType.MASTER);
        commands.put("!pause", tecCommand);

        tecCommand = new Command(this::unpauseBot, CommandType.MASTER);
        commands.put("!unpause", tecCommand);

        tecCommand = new Command(this::restartBot, CommandType.MASTER);
        commands.put("!martellstop", tecCommand);

        tecCommand = new Command(this::addPermission, CommandType.MASTER);
        commands.put("!разрешить", tecCommand);

        tecCommand = new Command(this::deletePermission, CommandType.MASTER);
        commands.put("!запретить", tecCommand);

        //--------------------------------------------------------------------------------------------
        tecCommand = new Command(this::getShazam, new CommandConfig(10, new HashSet(Set.of("EVERYONE")), CommandType.OTHER));
        commands.put("!шазам", tecCommand);

    }

    public boolean containsCommand(String command) {
        return commands.containsKey(command);
    }

    //------ИНФО-------

    private String getHpgTop(CommandArgumentDto args) {
        StringBuilder sb = new StringBuilder();
        sb.append("Топ: ");
        sb.append(MainController.getTop());
        sb.append(" @" + args.getUsername());
        return sb.toString();
    }

    private String getHpgInfo(CommandArgumentDto args) {
        String nick = CommandConstants.nicknames.get(args.getChannelname());
        String message = args.getMessage();
        if (message.matches("\\S+.*")) {
            nick = message;
            nick = CommandConstants.getNick(nick.toLowerCase());
            nick = CommandConstants.nicknames.get(nick);
            if (nick == null) {
                String msg = "Не понял. Ху? @" + args.getUsername();
                return msg;
            }
        }
        StringBuilder sb = new StringBuilder(nick + ": ");
        sb.append(MainController.getInfoAbout(nick));
        sb.append(" @" + args.getUsername());
        return sb.toString();
    }


    private String getHelp(CommandArgumentDto args) {
        String msg = "Доступные команды: !хпгтоп, !хпгинфо, !хпгинфо [ник], !событие";
        if (!commandConfigService.getFunConfig().isPaused()) {
            msg += ", !кто, !где, !когда, !паста, !анфиса [сообщение], " +
                    " !арт [смайл] или !арт [смайл] [чувствительность 0-100]";
        }
        return msg;
    }

    private String getEvent(CommandArgumentDto args) {
        String nick = CommandConstants.nicknames.get(args.getChannelname());
        String msg = MainController.getLastEvent(nick);

        return msg;
    }

    //------ФАН-------

    private String getPaste(CommandArgumentDto args) {
        return MainController.getPast();
    }

    private String chatBotAnswer(CommandArgumentDto args) {
        String message = args.getMessage();
        String msg = message.replaceFirst("!анфиса", "")
                .replaceAll("@martellx_bot", "");

        if (!msg.matches("\\s*?")) {
            String answer = MainController.getAnswerFromChatbot(msg) + " @" + args.getUsername();
            for (var bl : CommandConstants.blacklist
            ) {
                if (answer.contains(bl)) {
                    return null;
                }
            }
            return answer;
        }

        return null;
    }

    private String getComic(CommandArgumentDto args) {
        String answer = null;
        int i = 0;

        while (true) {
            boolean isBL = false;
            answer = MainController.getAnswerFromComicbot();
            for (var bl: CommandConstants.blacklist
            ) {
                if (answer.contains(bl)) {
                    isBL = true;
                    break;
                }
            }
            if ((answer.length() <= 500 || i > 15) && !isBL){
                break;
            }
            i++;
        }

        return answer;
    }



    private String getArt(CommandArgumentDto args) {
        String msg = args.getMessage();
        String channelName = args.getChannelname();
        if (msg.matches("\\S+.*")) {
            String emote = msg.replaceAll("^(\\S+).*", "$1");
            int threshold = -1;
            if (msg.matches("\\S+ \\d+.*")) {
                threshold = Integer.parseInt(msg.replaceAll("\\S+ (\\d+).*", "$1"));
            }
            String art = MainController.getArt(emote, channelName, threshold);
            return art;
        }
        return null;
    }

    private String who(CommandArgumentDto args) {
        String who = MainController.who();
        if (who.equals("ты")) {
            who = (who + " @" + args.getUsername());
        }

        return who;
    }

    private String when(CommandArgumentDto args) {
        return MainController.when();
    }

    private String where(CommandArgumentDto args) {
        return MainController.where();
    }

    Map<String, Integer> patronsRemain = new HashMap<>();

    private String roulette (CommandArgumentDto args) {
        String username = args.getUsername();
        int patrons = 7;
        if (patronsRemain.containsKey(username)) {
            patrons = patronsRemain.get(username);
        }
        Random rd = new Random();
        int patron = rd.nextInt(patrons);
        String result;
        if (patron == 0) {
            result = (7 - patrons + 1) + " патрон убил тебя pressF @" + username ;
            patronsRemain.remove(username);
        } else {
            patrons--;
            patronsRemain.put(username, patrons);
            result = "Ты выжил, количество оставшихся патронов в револьвере: " + patrons + " @" + username;
        }
        return result;
    }


    //------MODERATORS------

    Map<String, CommandType> commandTypeMap = Map.of("инфо", CommandType.INFO, "фан", CommandType.FUN);
    private String setDelay(CommandArgumentDto args) {
        String message = args.getMessage();
        String result = null;
        if (message.matches("^\\S+ \\d+.*")) {
            String type = message.replaceAll("^([^0-9^\\s]+)\\s.*$", "$1");
            int delay = Integer.parseInt(message.replaceAll("^[^0-9^\\s]+\\s(\\d+).*$", "$1"));
            if (commandTypeMap.containsKey(type)) {
                CommandType commandType = commandTypeMap.get(type);
                commandConfigService.getConfig(commandType).setDelay(delay);
                updateConfigsOfType(commandType);
                result = "Задержка команд типа \"" + type + "\" установлена на [" + delay + "] секунд";
            } else if (commands.containsKey(type)) {
                Command command = commands.get(type);
                command.getConfig().setDelay(delay);
                result = "Задержка команды \"" + type + "\" установлена на [" + delay + "] секунд";
            }
        }

        return result;

    }

    private String offCommand (CommandArgumentDto args) {
        String message = args.getMessage();
        String result = null;
        if (message.matches("^\\S+.*")) {
            String type = message.replaceAll("^([^0-9^\\s]+).*$", "$1");
            if (commandTypeMap.containsKey(type)) {
                CommandType commandType = commandTypeMap.get(type);
                commandConfigService.getConfig(commandType).setPaused(true);
                updateConfigsOfType(commandType);
                result = "Команды типа \"" + type + "\" отключены";
            } else if (commands.containsKey(type)) {
                Command command = commands.get(type);
                command.getConfig().setPaused(true);
                result = "Команда \"" + type + "\" отключена";
            } else if (type.equals("события")) {
                MainController.isCheckedEvents = false;
                result = "Проверка событий отключена";
            }
        }
        return result;
    }

    private String onCommand (CommandArgumentDto args) {
        String message = args.getMessage();
        String result = null;
        if (message.matches("^\\S+.*")) {
            String type = message.replaceAll("^([^0-9^\\s]+).*$", "$1");
            if (commandTypeMap.containsKey(type)) {
                CommandType commandType = commandTypeMap.get(type);
                commandConfigService.getConfig(commandType).setPaused(false);
                updateConfigsOfType(commandType);
                result = "Команды типа \"" + type + "\" включены";
            } else if (commands.containsKey(type)) {
                Command command = commands.get(type);
                command.getConfig().setPaused(false);
                result = "Команда \"" + type + "\" включена";
            } else if (type.equals("события")) {
                MainController.isCheckedEvents = true;
                result = "Проверка событий включена";
            }
        }
        return result;
    }

    //------MASTER------
    private String joinToChannel(CommandArgumentDto args) {
        String message = args.getMessage();
        String result = "Не понял прикола";
        if (message.matches("\\S+.*")) {
            String joinTo = message.replaceFirst("\\S+", "$1");
            String joinToNick = CommandConstants.getNick(joinTo);
            if (joinToNick != null) {
                result = MainController.joinTo(joinToNick);
            } else {
                result = MainController.joinTo(joinTo);
            }
        }
        return result;
    }

    private String pauseBot(CommandArgumentDto args) {
        commandConfigService.getConfig(CommandType.INFO).setPaused(true);
        commandConfigService.getConfig(CommandType.FUN).setPaused(true);
        updateConfigsOfType(CommandType.INFO);
        updateConfigsOfType(CommandType.FUN);
        String result = "чилю :)";
        return result;
    }

    private String unpauseBot(CommandArgumentDto args) {
        commandConfigService.getConfig(CommandType.INFO).setPaused(true);
        commandConfigService.getConfig(CommandType.FUN).setPaused(true);
        updateConfigsOfType(CommandType.INFO);
        updateConfigsOfType(CommandType.FUN);
        String result = "работаем";
        return result;
    }

    private String restartBot(CommandArgumentDto args) {
        MainController.isStopped = true;
        return "Перезапускаюсь(наверное) peepoRip";
    }

    private String addPermission(CommandArgumentDto args) {
        String message = args.getMessage();
        String result = null;
        if (message.matches("^\\S+ \\S+.*")) {
            String type = message.replaceAll("^([^0-9^\\s]+)\\s.*$", "$1");

            String perm = message.replaceAll("^[^0-9^\\s]+\\s(\\S+).*$", "$1");
            if (commandTypeMap.containsKey(type)) {
                CommandType commandType = commandTypeMap.get(type);
                commandConfigService.getConfig(commandType).addPermission(perm.toUpperCase());
                updateConfigsOfType(commandType);
                result = "\"" + perm + "\" разрешено пользоваться командами типа \"" + type + "\"";
            } else if (commands.containsKey(type)) {
                Command command = commands.get(type);
                command.getConfig().addPermission(perm.toUpperCase());
                result = "" + perm + "\" разрешено пользоваться командой \"" + type + "\"";
            }
        }

        return result;
    }

    private String deletePermission(CommandArgumentDto args) {
        String message = args.getMessage();
        String result = null;
        if (message.matches("^\\S+ \\S+.*")) {
            String type = message.replaceAll("^([^0-9^\\s]+)\\s.*$", "$1");

            String perm = message.replaceAll("^[^0-9^\\s]+\\s(\\S+).*$", "$1");
            if (commandTypeMap.containsKey(type)) {
                CommandType commandType = commandTypeMap.get(type);
                commandConfigService.getConfig(commandType).deletePermission(perm.toUpperCase());
                updateConfigsOfType(commandType);
                result = "Удалено разрешение \"" + perm + "\" для команд типа \"" + type + "\"";
            } else if (commands.containsKey(type)) {
                Command command = commands.get(type);
                command.getConfig().deletePermission(perm.toUpperCase());
                result = "Удалено разрешение \"" + perm + "\" для команды \"" + type + "\"";
            }
        }

        return result;
    }

    private String getShazam(CommandArgumentDto args) {
        String answer = MainController.getShazam(args.getChannelname());
        return answer + " @" + args.getUsername();
    }

    private void updateConfigsOfType(CommandType type) {
        for (var command:commands.values()
             ) {
            if (command.getConfig().getType() == type) {
                command.setConfig(commandConfigService.getConfigClone(type));
            }
        }
    }





}
