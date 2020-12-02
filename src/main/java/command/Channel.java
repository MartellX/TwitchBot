package command;

import com.github.twitch4j.chat.events.channel.IRCMessageEvent;
import controllers.MainController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//TODO собственно сделать
public class Channel {

    private String name;
    private int id;
    private boolean isLive;
    Map<String, CommandConfig> commands;

    private CommandConfigService commandConfigService;
    private CommandExecutor executor;
    private boolean isAutoShazam = false;
    private boolean isShazaming = false;

    private HashMap<String, String> channelData = new HashMap<>();

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

        public Builder setAutoShazam(boolean isAuto) {
            channel.isAutoShazam = isAuto;
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

            channel.executor.setChannel(channel);
            channel.commands.clear();
            for (CommandConfig config:channel.executor.getConfigs()
                 ) {
                channel.commands.put(config.getName(), config);
            }
            channel.channelData.put("last_result", "");
            initListenThreads();
            return channel;
        }

        private void initListenThreads() {
            if (channel.isAutoShazam) {
                channel.startAutoShazam();
            }
        }

    }

    Thread shazamListen;

    private void startAutoShazam() {
        shazamListen = new Thread(() ->
        {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isShazaming = true;
            long lastCheckTime = 0;
            while (this.isAutoShazam) {

                long thisCheckTime = System.currentTimeMillis();
                if (thisCheckTime - lastCheckTime >= 120 * 1000) {
                    isLive();
                    if (!isLive) {
                        isShazaming = false;
                        break;
                    }
                    lastCheckTime = thisCheckTime;
                }
                String lastResult = this.channelData.get("last_result");
                String result = MainController.getShazamV2(this.name);
                if (result != null && !result.equals(lastResult)) {
                    MainController.sendMessage("Сейчас играет: " + result, this.name);
                    this.channelData.put("last_result", result);
                } else {
                    System.out.println("[" + name + "]" + " Нет результатов");
                }
                try {
                    if (result == null) {
                        TimeUnit.SECONDS.sleep(15);
                    } else {
                        TimeUnit.SECONDS.sleep(30);
                    }
                } catch (InterruptedException e) {
//                e.printStackTrace();
                    isShazaming = false;
                }
            }
        });
        shazamListen.start();
    }

    public boolean isLive() {
        assert id != 0;
        boolean check = MainController.checkOnLive(String.valueOf(id));
        isLive = check;
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public void setAutoShazam(boolean autoShazam) {
        if (!shazamListen.isAlive() && autoShazam) {
            startAutoShazam();
        }

        if (shazamListen.isAlive() && !autoShazam) {
            shazamListen.interrupt();
        }
        isAutoShazam = autoShazam;
    }

    public boolean isAutoShazam() {
        return isAutoShazam;
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


