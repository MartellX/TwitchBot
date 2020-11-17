package command;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import constants.CommandConstants;
import controllers.MainController;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class CommandExecutor {

    private static final Map<String, Command> defaultCommands = new HashMap<>();

    private Map<String, Command> commands = new HashMap<>();
    private CommandConfigService commandConfigService;

    static {
        for (Method method:CommandExecutor.class.getDeclaredMethods()
        ) {
            if (method.isAnnotationPresent(CommandAnnotation.class)) {
                CommandAnnotation annotation = method.getAnnotation(CommandAnnotation.class);

                if (!annotation.isDisabled()) {
                    Command command = new Command(null, annotation.type());
                    if (annotation.delay() != -1) {
                        command.getConfig().setDelay(annotation.delay());
                    }

                    command.getConfig().setPaused(annotation.isPaused());
                    if (annotation.permissions().length != 0) {
                        HashSet<String> permissions = new HashSet(Arrays.asList(annotation.permissions()));
                        command.getConfig().setNeededPermissions(permissions);
                    }
                    command.setMethod(method);
                    command.setName(annotation.name());
                    command.getConfig().setName(annotation.name());
                    defaultCommands.put(annotation.name(), command);
                    for (String name: annotation.names()
                         ) {
                        defaultCommands.put(name, command);
                    }
                }

            }
        }


    }

    public String execute(String commandTag, String channelname, String username, Set<String> userPermissions,
                          String message, IRCMessageEvent messageEvent){
        Command command = commands.get(commandTag);
        synchronized (command) {
            Function<CommandArgumentDto, String> commandFunction = command.getCommand();
            Method commandMethod = command.getMethod();
            Set neededPermissions = command.getConfig().getNeededPermissions();
            String emotes = null;
            if (messageEvent.getTagValue("emotes").isPresent()) {
                emotes = messageEvent.getTagValue("emotes").get();
            }
            int neededDelay = command.getConfig().getDelay();
            long lastExecute = command.getLastExecution();
            long currentTime = System.currentTimeMillis();
            CommandArgumentDto args = new CommandArgumentDto(channelname, username, userPermissions, message, emotes);
            String result = null;
            if (Collections.disjoint(userPermissions, neededPermissions)) {
                result = null;
            } else if ((currentTime - lastExecute) < neededDelay * 1000 || command.getConfig().isPaused()) {
                result = null;
            } else {
                if (commandFunction != null) {
                    result = commandFunction.apply(args);
                } else {
                    try {
                        result = (String) commandMethod.invoke(this, args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                if (result != null) {
                    command.setLastExecution(System.currentTimeMillis());
                }
            }
            return result;
        }
    }

    public CommandExecutor() {

        for (var comm : defaultCommands.entrySet()
             ) {
            String alias = comm.getKey();
            Command commandDefault = comm.getValue();
            Command command = commandDefault.clone();
            commands.put(alias, command);
        }

//        commandConfigService = CommandConfigService.getDefault();
//        Command tecCommand;
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

//
//        tecCommand = new Command(this::getTimeout, CommandType.INFO);
//        commands.put("!кд", tecCommand);
//
//        //----------------------------------------------------------------------------------------------
//
//
//        tecCommand = new Command(this::getAnek, new CommandConfig(60, new HashSet(Set.of("EVERYONE")), CommandType.FUN));
//        commands.put("!анек", tecCommand);
//        commands.put("!pogo", tecCommand);
//
//        tecCommand = new Command(this::getPaste, new CommandConfig(60, new HashSet(Set.of("EVERYONE")), true, CommandType.FUN));
//        commands.put("!паста", tecCommand);
//
//        tecCommand = new Command(this::chatBotAnswer, new CommandConfig(30, new HashSet(Set.of("EVERYONE")), CommandType.FUN));
//        commands.put("!анфиса", tecCommand);
//
//
//        tecCommand = new Command(this::who, CommandType.FUN);
//        commands.put("!кто", tecCommand);
//
//        tecCommand = new Command(this::where, CommandType.FUN);
//        commands.put("!где", tecCommand);
//
//        tecCommand = new Command(this::when, CommandType.FUN);
//        commands.put("!когда", tecCommand);
//
//        tecCommand = new Command(this::getArt, CommandType.FUN);
//        commands.put("!арт", tecCommand);
//
//        tecCommand = new Command(this::roulette, CommandType.FUN);
//        commands.put("!рулетка", tecCommand);
//
//        //----------------------------------------------------------------------------------------------
//
//        tecCommand = new Command(this::setDelay, CommandType.MOD);
//        commands.put("!задержка", tecCommand);
//
//        tecCommand = new Command(this::offCommand, CommandType.MOD);
//        commands.put("!выкл", tecCommand);
//
//        tecCommand = new Command(this::onCommand, CommandType.MOD);
//        commands.put("!вкл", tecCommand);
//
//
//        //----------------------------------------------------------------------------------------------
//
//        tecCommand = new Command(this::joinToChannel, CommandType.MASTER);
//        commands.put("!jointo", tecCommand);
//
//        tecCommand = new Command(this::pauseBot, CommandType.MASTER);
//        commands.put("!pause", tecCommand);
//
//        tecCommand = new Command(this::unpauseBot, CommandType.MASTER);
//        commands.put("!unpause", tecCommand);
//
//        tecCommand = new Command(this::restartBot, CommandType.MASTER);
//        commands.put("!martellstop", tecCommand);
//
//        tecCommand = new Command(this::addPermission, CommandType.MASTER);
//        commands.put("!разрешить", tecCommand);
//
//        tecCommand = new Command(this::deletePermission, CommandType.MASTER);
//        commands.put("!запретить", tecCommand);
//
//        //--------------------------------------------------------------------------------------------
//        tecCommand = new Command(this::getShazam, new CommandConfig(10, new HashSet(Set.of("EVERYONE")), CommandType.OTHER));
//        commands.put("!шазам", tecCommand);


    }

    public CommandExecutor(CommandConfigService configService, Map<String, Command> commands) {
        this.commandConfigService = configService;
        this.commands.putAll(commands);

    }

    public boolean containsCommand(String command) {
        return commands.containsKey(command);
    }


    private void updateConfigsOfType(CommandType type) {
        for (var command:commands.values()
        ) {
            if (command.getConfig().getType() == type) {
                command.setConfig(commandConfigService.getConfigClone(type));
            }
        }
    }

    public Command getCommand(String command) {
        return commands.get(command);
    }
    public List<CommandConfig> getConfigs () {
        HashSet<CommandConfig> configSet = new HashSet<>();
        commands.values().forEach(c -> configSet.add(c.getConfig()));
        return new ArrayList<>(configSet);
    }

    public void setCommandConfigService(CommandConfigService commandConfigService) {
        this.commandConfigService = commandConfigService;
    }

    public CommandConfigService getCommandConfigService() {
        return commandConfigService;
    }



    // НИЖЕ ТОЛЬКО ФУНКЦИИ ДЛЯ КОМАНД

    //------ИНФО-------


    @CommandAnnotation(name = "!хпгтоп",
            type = CommandType.INFO,
            isPaused = true)
    private String getHpgTop(CommandArgumentDto args) {
        StringBuilder sb = new StringBuilder();
        sb.append("Топ: ");
        sb.append(MainController.getTop());
        sb.append(" @" + args.getUsername());
        return sb.toString();
    }

    @CommandAnnotation(name = "!хпгинфо",
            type = CommandType.INFO,
            isPaused = true,
            isDisabled = true)
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

    @CommandAnnotation(name = "!помощь", type = CommandType.INFO, isPaused = true,
            isDisabled = true)
    private String getHelp(CommandArgumentDto args) {
        String msg = "Доступные команды: !хпгтоп, !хпгинфо, !хпгинфо [ник], !событие";
        if (!commandConfigService.getFunConfig().isPaused()) {
            msg += ", !кто, !где, !когда, !паста, !анфиса [сообщение], " +
                    " !арт [смайл] или !арт [смайл] [чувствительность 0-100]";
        }
        return msg;
    }

    @CommandAnnotation(name = "!событие", type = CommandType.INFO, isPaused = true,
            isDisabled = true)
    private String getEvent(CommandArgumentDto args) {
        String nick = CommandConstants.nicknames.get(args.getChannelname());
        String msg = MainController.getLastEvent(nick);

        return msg;
    }

    @CommandAnnotation(name = "!шазам", type = CommandType.INFO, delay = 20)
    private String getShazam(CommandArgumentDto args) {
        String answer = MainController.getShazam(args.getChannelname());
        if (answer.contains("No result")) {
            MainController.sendMessage(answer + " @" + args.getUsername(), args.getChannelname());
            return null;
        }
        return answer + " @" + args.getUsername();
    }

    @CommandAnnotation(name = "!кд", type = CommandType.INFO, delay = 10)
    private String getTimeout(CommandArgumentDto args) {
        String message = args.getMessage();
        String result = null;
        if (message.matches("^\\S+.*")) {
            String type = message.replaceAll("^([^0-9^\\s]+).*$", "$1");
            type = type.toLowerCase();
            if (commands.containsKey(type)) {
                Command command = commands.get(type);
                if (command.getConfig().isPaused()) {
                    result = "Команда " + type + " отключена";
                } else {
                    int time = command.getConfig().getDelay();
                    int remainTime = (int) (time - (System.currentTimeMillis() - command.getLastExecution())/1000);
                    result = "У команды " + type + " задержка " + time + " секунд " + (remainTime >= 0 ? ", осталось " + remainTime : ", команда сейчас доступна") + " @" + args.getUsername();
                }
            }
        }

        return result;
    }

    //------ФАН-------

    @CommandAnnotation(name = "!паста", type = CommandType.FUN, isPaused = true)
    private String getPaste(CommandArgumentDto args) {
        String result = MainController.getPast(args.getMessage());
        if (result == null) {
            //MainController.sendMessage("Не удалось найти пасту " + args.getUsername(), args.getChannelname());
            result = "Не удалось найти пасту @" + args.getUsername();
        }
        return result;
    }

    @CommandAnnotation(name = "!анфиса", type = CommandType.FUN, delay = 30, isPaused = false)
    private String chatBotAnswer(CommandArgumentDto args) {
        String message = args.getMessage();
        String msg = message.replaceFirst("!анфиса", "")
                .replaceAll("@martellx_bot", "");

        if (!msg.matches("\\s*?")) {
            String answer = MainController.getAnswerFromChatbot(msg) + " @" + args.getUsername();
            for (var bl : CommandConstants.blacklist
            ) {
                String searchbl = "[\\s\\S]*" + bl + "[\\s\\S]*";
                if (answer.toLowerCase().matches(searchbl)) {
                    return null;
                }
            }
            return answer;
        }

        return null;
    }


    @CommandAnnotation(name = "!арт", type = CommandType.FUN, isPaused = true)
    private String getArt(CommandArgumentDto args) {
        String msg = args.getMessage();
        String channelName = args.getChannelname();
        String emoteInfo = args.getEmotesInfo();
        String emoteID = null;
        if (emoteInfo != null) {
            emoteInfo = emoteInfo.replaceAll("\\\\.*", "");
            emoteID = emoteInfo.replaceAll(":.*", "");
        }
        if (msg.matches("\\S+.*")) {
            String emote = msg.replaceAll("^(\\S+).*", "$1");
            int threshold = -1;
            if (msg.matches("\\S+ \\d+.*")) {
                threshold = Integer.parseInt(msg.replaceAll("\\S+ (\\d+).*", "$1"));
            }
            String art;
            if (emoteID == null) {
                art = MainController.getArt(emote, channelName, threshold);
            } else {
                art = MainController.getArt(emoteID, threshold);
            }
            return art;
        }
        return null;
    }

    @CommandAnnotation(name = "!кто", type = CommandType.FUN, isPaused = true)
    private String who(CommandArgumentDto args) {
        String who = MainController.who();
        if (who.equals("ты")) {
            who = (who + " @" + args.getUsername());
        }

        return who;
    }

    @CommandAnnotation(name = "!где", type = CommandType.FUN, isPaused = true)
    private String when(CommandArgumentDto args) {
        return MainController.when();
    }

    @CommandAnnotation(name = "!когда", type = CommandType.FUN, isPaused = true)
    private String where(CommandArgumentDto args) {
        return MainController.where();
    }

    Map<String, Integer> patronsRemain = new HashMap<>();

    @CommandAnnotation(name = "!рулетка", type = CommandType.FUN, isPaused = true)
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
            result = (7 - patrons + 1) + " патрон убил тебя F @" + username ;
            patronsRemain.remove(username);
        } else {
            patrons--;
            patronsRemain.put(username, patrons);
            result = "Ты выжил, количество оставшихся патронов в револьвере: " + patrons + " @" + username;
        }
        return result;
    }

    @CommandAnnotation(name = "!анек", type = CommandType.FUN, isPaused = true)
    private String getAnek(CommandArgumentDto args) {
        String result = MainController.getAnek();
        return result;
    }


    //------MODERATORS------

    Map<String, CommandType> commandTypeMap = Map.of("инфо", CommandType.INFO, "фан", CommandType.FUN);

    @CommandAnnotation(name = "!задержка", type = CommandType.MOD)
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

    @CommandAnnotation(name = "!выкл", type = CommandType.MOD)
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

    @CommandAnnotation(name = "!вкл", type = CommandType.MOD)
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
    @CommandAnnotation(name = "!jointo", type = CommandType.MASTER)
    private String joinToChannel(CommandArgumentDto args) {
        String message = args.getMessage();
        String result = "Не понял прикола";
        if (message.matches("\\S+.*")) {
            String joinTo = message.replaceFirst("(\\S+).*", "$1");
            String joinToNick = CommandConstants.getNick(joinTo);
            if (joinToNick != null) {
                result = MainController.joinTo(joinToNick);
            } else {
                result = MainController.joinTo(joinTo);
            }
        }
        return result;
    }

    @CommandAnnotation(name = "!pause", type = CommandType.MASTER)
    private String pauseBot(CommandArgumentDto args) {
        commandConfigService.getConfig(CommandType.INFO).setPaused(true);
        commandConfigService.getConfig(CommandType.FUN).setPaused(true);
        updateConfigsOfType(CommandType.INFO);
        updateConfigsOfType(CommandType.FUN);
        String result = "чилю :)";
        return result;
    }

    @CommandAnnotation(name = "!unpause", type = CommandType.MASTER)
    private String unpauseBot(CommandArgumentDto args) {
        commandConfigService.getConfig(CommandType.INFO).setPaused(false);
        commandConfigService.getConfig(CommandType.FUN).setPaused(false);
        updateConfigsOfType(CommandType.INFO);
        updateConfigsOfType(CommandType.FUN);
        String result = "работаем";
        return result;
    }

    @CommandAnnotation(name = "!restart", type = CommandType.MASTER)
    private String restartBot(CommandArgumentDto args) {
        MainController.isStopped = true;
        return "Перезапускаюсь(наверное) peepoRip";
    }

    @CommandAnnotation(name = "!разрешить", type = CommandType.MASTER)
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

    @CommandAnnotation(name = "!запретить", type = CommandType.MASTER)
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
    

    private String sendPmMessage(CommandArgumentDto args){
        MainController.sendPMmessage(args.getUsername(), args.getMessage() + " @" + args.getUsername());
        return "Сообщение отправлено";
    }

}
