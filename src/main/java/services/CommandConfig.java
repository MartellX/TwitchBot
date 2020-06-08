package services;


import java.util.HashSet;
import java.util.Set;

public class CommandConfig {
    private int delay;
    private Set<String> neededPermissions;
    private boolean isPaused = false;

    public CommandConfig(int delay, Set neededPermissions) {
        this.delay = delay;
        this.neededPermissions = neededPermissions;
    }

    public CommandConfig(int delay, Set neededPermissions, boolean isPaused) {
        this.delay = delay;

        this.neededPermissions = neededPermissions;
        this.isPaused = isPaused;
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

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    protected CommandConfig clone(){
        CommandConfig clone = new CommandConfig(delay, new HashSet(neededPermissions), isPaused);
        return clone;
    }

    @Override
    public String toString() {
        return "CommandConfig{" +
                "delay=" + delay +
                ", neededPermissions=" + neededPermissions +
                '}';
    }
}
