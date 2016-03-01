package com.github.xCyanide.commands;

import com.github.xCyanide.ClaimLevels;
import com.github.xCyanide.Utils.Lang;
import com.gmail.nossr50.api.ExperienceAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Redeem implements CommandExecutor
{
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
            return true;
        }

        Player p = (Player) sender;
        if (args.length <= 1)
        {
            p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "/redeem <skill> <levels>");
        }
        else if (args.length == 2)
        {

            String skillType = args[0];
            if (!(skillType.equalsIgnoreCase("taming") || skillType.equalsIgnoreCase("swords")
                    || skillType.equalsIgnoreCase("unarmed")
                    || skillType.equalsIgnoreCase("archery")
                    || skillType.equalsIgnoreCase("axes")
                    || skillType.equalsIgnoreCase("acrobatics")
                    || skillType.equalsIgnoreCase("fishing")
                    || skillType.equalsIgnoreCase("excavation")
                    || skillType.equalsIgnoreCase("mining")
                    || skillType.equalsIgnoreCase("herbalism")
                    || skillType.equalsIgnoreCase("repair")
                    || skillType.equalsIgnoreCase("woodcutting"))
                    || skillType.equalsIgnoreCase("alchemy"))
            {
                p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "That is not a skill");
                return true;
            }

            else if (!(p.hasPermission("mcmmo.skills." + skillType)))
            {
                p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                return true;
            }

            checkCap(p, args);

        }
        else
        {
            p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "/redeem <skill> <levels>");
            return true;
        }
        return true;
    }

    public boolean checkCap(Player p, String[] args)
    {
        String skillType = args[0];
        int cap = ExperienceAPI.getLevelCap(skillType);
        int levels;
        try
        {
            levels = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex)
        {
            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
            return true;
        }

        int oldamount = ClaimLevels.dm.getData().getInt(p.getUniqueId().toString() + ".credits");

        if (oldamount < levels)
        {
            p.sendMessage(Lang.PREFIX.toString() + ChatColor.RED + "You do not have enough credits!");
            return true;
        }
        if (levels <= 0)
        {
            p.sendMessage(Lang.PREFIX.toString() + Lang.POSITIVE_NUMBER);
            return true;
        }
        if (ExperienceAPI.getLevel(p, skillType) + levels > cap)
        {
            p.sendMessage(Lang.PREFIX.toString() + ChatColor.RED + "You have reached the maximum level for " + skillType);
            return true;
        }

        int newamount = oldamount - levels;

        if (newamount == 0)
        {
            ClaimLevels.dm.getData().set(p.getUniqueId().toString(), null);
        }
        else
        {
            ClaimLevels.dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
        }
        ClaimLevels.dm.saveData();
        ExperienceAPI.addLevel(p, skillType, levels);
        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels
                + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
        return true;
    }
}
