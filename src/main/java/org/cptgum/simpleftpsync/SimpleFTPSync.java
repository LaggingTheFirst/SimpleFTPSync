package org.cptgum.simpleftpsync;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleFTPSync extends JavaPlugin {
    private SyncTask syncTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("sfs").setExecutor(new SyncCommand(this));

        syncTask = new SyncTask(this);
        syncTask.runTaskTimer(this, 0L, getConfig().getInt("sync-interval") * 20L);
    }

    @Override
    public void onDisable() {
        syncTask.cancel();
    }

    public FileConfiguration getPluginConfig() {
        return getConfig();
    }
}
