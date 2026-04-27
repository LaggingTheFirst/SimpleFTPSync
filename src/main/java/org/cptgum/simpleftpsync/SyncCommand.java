package org.cptgum.simpleftpsync;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SyncCommand implements CommandExecutor {
    private final SimpleFTPSync plugin;

    public SyncCommand(SimpleFTPSync plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("syncfolder.reload")) {
                plugin.reloadPlugin();
                sender.sendMessage(ChatColor.GREEN + "Config reloaded.");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
            }

            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("run")) {
            if (sender.hasPermission("syncfolder.run")) {
                plugin.runSyncNow();
                sender.sendMessage(ChatColor.GREEN + "Manual sync started.");
            } else {
                sender.sendMessage(ChatColor.RED + "You do not have permission for this command.");
            }

            return true;
        }

        return false;
    }
}
