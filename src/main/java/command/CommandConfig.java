package command;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CommandConfig {
    private int id;
    private String name;
    private int delay;
    private Set<String> neededPermissions;
    private boolean isPaused = false;
    private CommandType type;

    public CommandConfig(int delay, Set neededPermissions, CommandType type) {
        this.delay = delay;
        this.neededPermissions = neededPermissions;
        this.type = type;
    }

    public CommandConfig(int delay, Set neededPermissions, boolean isPaused) {
        this.delay = delay;
        this.neededPermissions = neededPermissions;
        this.isPaused = isPaused;
    }

    public CommandConfig(int delay, Set neededPermissions, boolean isPaused, CommandType type) {
        this(delay, neededPermissions, isPaused);
        this.type = type;
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

    public int getDelay() {
        return delay;
    }

    public Set<String> getNeededPermissions() {
        return neededPermissions;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public void addPermission (String permission) {
        neededPermissions.add(permission);
    }

    public void deletePermission(String permission) {
        neededPermissions.remove(permission);
    }
    public void setNeededPermissions(Set<String> neededPermissions) {
        this.neededPermissions = neededPermissions;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    protected CommandConfig clone(){
        return new CommandConfig(delay, new HashSet(neededPermissions), isPaused, type);
    }

    @Override
    public String toString() {
        return "CommandConfig{" +
                "delay=" + delay +
                ", neededPermissions=" + neededPermissions +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandConfig config = (CommandConfig) o;
        return getId() == config.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
