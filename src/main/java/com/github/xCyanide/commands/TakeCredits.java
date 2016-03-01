package com.github.xCyanide.commands;

import com.github.xCyanide.ClaimLevels;
import com.github.xCyanide.Utils.Lang;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TakeCredits implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if (sender.hasPermission("credits.take"))
        {
            if (args.length <= 1)
            {
                sender.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "/takecredits <player> <credits>");
                return true;
            }

            Player target = Bukkit.getServer().getPlayer(args[0]);
            OfflinePlayer offlineTarget = Bukkit.getServer().getOfflinePlayer(args[0]);
            if (target == null && offlineTarget.hasPlayedBefore())
            {
                String targetPlayer = offlineTarget.getName().toLowerCase();
                String UUID = ClaimLevels.getUUIDFromName(targetPlayer);
                try
                {
                    Integer.parseInt(args[1]);
                }
                catch (NumberFormatException ex)
                {
                    sender.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                    return true;
                }
                int levels = Integer.parseInt(args[1]);
                if (levels <= 0)
                {
                    sender.sendMessage(Lang.PREFIX.toString() + Lang.POSITIVE_NUMBER);
                    return true;
                }
                int oldAmount = ClaimLevels.dm.getData().getInt(UUID + ".credits");
                if (levels <= oldAmount)
                {
                    int newAmount = oldAmount - levels;
                    ClaimLevels.dm.getData().set(UUID + ".credits", newAmount);
                    ClaimLevels.dm.saveData();
                    sender.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have taken " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels from " + targetPlayer);
                    return true;
                }
                else
                {
                    sender.sendMessage(Lang.PREFIX.toString() + ChatColor.YELLOW + args[0] + ChatColor.GREEN + " already has " + ChatColor.GOLD + "0" + ChatColor.GREEN + " credits");
                    return true;
                }
            }
            else if (target == null && !offlineTarget.hasPlayedBefore())
            {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER);
            }
            else if (target != null)
            {
                int credits;
                String targetPlayer = target.getName().toLowerCase();
                try
                {
                    credits = Integer.parseInt(args[1]);
                }
                catch (NumberFormatException ex)
                {
                    sender.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                    return true;
                }
                if (credits <= 0)
                {
                    sender.sendMessage(Lang.PREFIX.toString() + Lang.POSITIVE_NUMBER);
                    return true;
                }

                if (ClaimLevels.dm.getData().contains(target.getUniqueId().toString()))
                {
                    int oldAmount = ClaimLevels.dm.getData().getInt(target.getUniqueId().toString() + ".credits");
                    if (credits <= oldAmount)
                    {
                        int newAmount = oldAmount - credits;
                        ClaimLevels.dm.getData().set(target.getUniqueId().toString() + ".credits", newAmount);
                        ClaimLevels.dm.saveData();
                        sender.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have taken " + ChatColor.GOLD + credits + " credits from " + ChatColor.GREEN + targetPlayer);
                        return true;
                    }
                    else
                    {
                        sender.sendMessage(Lang.PREFIX.toString() + ChatColor.YELLOW + args[0] + ChatColor.GREEN + " already has " + ChatColor.GOLD + "0" + ChatColor.GREEN + "credits");
                        return true;
                    }
                }
            }
        }
        sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMS);
        return true;
    }
}
