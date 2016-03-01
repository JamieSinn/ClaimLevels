package com.github.xCyanide.commands;

import com.github.xCyanide.ClaimLevels;
import com.github.xCyanide.Utils.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Credits implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if (sender instanceof Player)
        {
            Player p = (Player) sender;
            if (args.length == 0)
            {
                p.sendMessage(Lang.CREDITPREFIX.toString() + ChatColor.GREEN + "You have " +
                        ChatColor.GOLD +
                        (ClaimLevels.dm.getData().contains(p.getUniqueId().toString())
                                ? ClaimLevels.dm.getData().getInt(p.getUniqueId().toString() + ".credits") : 0)
                        + ChatColor.GREEN + " credits");
                return true;
            }
            else if (args.length == 1)
            {
                if (p.hasPermission("credits.inspect"))
                {
                    String targetName = args[0];
                    String UUID = ClaimLevels.getUUIDFromName(targetName);


                    p.sendMessage(Lang.CREDITPREFIX.toString() + ChatColor.YELLOW + args[0].toLowerCase()
                            + ChatColor.GREEN + " has " + ChatColor.GOLD +
                            (UUID != null ? ClaimLevels.dm.getData().getInt(args[0].toLowerCase() + ".credits") : 0)
                            + ChatColor.GREEN + " credits");

                }
                else if (!(p.hasPermission("credits.inspect")))
                {
                    p.sendMessage(Lang.CREDITPREFIX.toString() + Lang.NO_PERMS);
                }
                return true;
            }
            else
            {
                p.sendMessage(Lang.CREDITPREFIX.toString() + ChatColor.GREEN + "/credits <player>");
                return true;
            }
        }
        sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
        return true;
    }
}
