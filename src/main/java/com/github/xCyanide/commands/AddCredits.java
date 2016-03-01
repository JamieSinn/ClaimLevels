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

public class AddCredits implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if (sender.hasPermission("credits.add"))
        {
            if (args.length <= 1)
            {
                sender.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "/addcredits <player> <levels>");
            }
            else if (args.length == 2)
            {
                Player target = Bukkit.getServer().getPlayer(args[0]);
                OfflinePlayer offlineTarget = Bukkit.getServer().getOfflinePlayer(args[0]);
                if (target == null && offlineTarget.hasPlayedBefore())
                {
                    String targetPlayer = offlineTarget.getName().toLowerCase();
                    String UUID = ClaimLevels.getUUIDFromName(targetPlayer);
                    int levels;
                    try
                    {
                        levels = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException ex)
                    {
                        sender.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                        return true;
                    }
                    if (levels <= 0)
                    {
                        sender.sendMessage(Lang.PREFIX.toString() + Lang.POSITIVE_NUMBER);
                        return true;
                    }
                    int oldAmount = ClaimLevels.dm.getData().getInt(UUID + ".credits");
                    int newAmount = oldAmount + levels;
                    ClaimLevels.dm.getData().set(UUID + ".credits", newAmount);
                    ClaimLevels.dm.saveData();
                    sender.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have given " + targetPlayer + " " + ChatColor.GOLD + levels + ChatColor.GREEN + " credits");
                    return true;
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
                    if (!ClaimLevels.dm.getData().contains(target.getUniqueId().toString()))
                    {
                        ClaimLevels.dm.getData().set(target.getUniqueId().toString() + ".credits", ClaimLevels.dm.getData().getInt("startAmount"));
                    }
                    int oldAmount = ClaimLevels.dm.getData().getInt(target.getUniqueId().toString() + ".credits");
                    int newAmount = oldAmount + credits;
                    ClaimLevels.dm.getData().set(target.getUniqueId().toString() + ".credits", newAmount);
                    ClaimLevels.dm.saveData();
                    sender.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have given " + targetPlayer + " " + ChatColor.GOLD + credits + ChatColor.GREEN + " credits");
                    target.sendMessage(ChatColor.GREEN + "You have received " + ChatColor.GOLD + credits + ChatColor.GREEN + " credits");
                    target.sendMessage(ChatColor.GREEN + "Do /credits to check how many credits you have");
                    return true;
                }
            }
            else
            {
                sender.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "/addcredits <player> <credits>");
            }
        }

        sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMS);
        return true;
    }
}
