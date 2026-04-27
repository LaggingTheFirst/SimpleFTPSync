package org.cptgum.simpleftpsync;

import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleFTPSync extends JavaPlugin {
    private SyncTask syncTask;
    private final AtomicBoolean syncInProgress = new AtomicBoolean(false);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (registerCommands()) {
            startSyncTask();
        }
    }

    @Override
    public void onDisable() {
        stopSyncTask();
    }

    public FileConfiguration getPluginConfig() {
        return getConfig();
    }

    public void reloadPlugin() {
        reloadConfig();
        startSyncTask();
    }

    public void runSyncNow() {
        getServer().getScheduler().runTaskAsynchronously(this, new SyncTask(this));
    }

    public boolean beginSyncRun() {
        return syncInProgress.compareAndSet(false, true);
    }

    public void endSyncRun() {
        syncInProgress.set(false);
    }

    private boolean registerCommands() {
        PluginCommand command = getCommand("sfs");

        if (command == null) {
            getLogger().severe("Command 'sfs' is missing from plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        command.setExecutor(new SyncCommand(this));
        return true;
    }

    private void startSyncTask() {
        stopSyncTask();

        long syncIntervalSeconds = Math.max(1L, getConfig().getLong("sync-interval", 300L));
        long initialDelay = getConfig().getBoolean("sync-on-startup", true) ? 0L : syncIntervalSeconds * 20L;
        syncTask = new SyncTask(this);
        syncTask.runTaskTimerAsynchronously(this, initialDelay, syncIntervalSeconds * 20L);
    }

    private void stopSyncTask() {
        if (syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }
    }
}
