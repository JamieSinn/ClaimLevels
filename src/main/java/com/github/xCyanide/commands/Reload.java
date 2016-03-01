package com.github.xCyanide.commands;

import com.github.xCyanide.ClaimLevels;
import com.github.xCyanide.Utils.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Reload implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if (sender.hasPermission("claimlevels.reload"))
        {
            ClaimLevels.getInstance().reloadConfig();
            ClaimLevels.dm.reloadData();
            sender.sendMessage(ChatColor.GREEN + "ClaimLevels config reloaded");
            return true;
        }

        sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMS);
        return true;
    }
}
