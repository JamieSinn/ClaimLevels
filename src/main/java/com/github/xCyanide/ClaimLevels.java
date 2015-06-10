package com.github.xCyanide;

import com.github.xCyanide.Utils.DataManager;
import com.github.xCyanide.Utils.Lang;
import com.github.xCyanide.Utils.UUIDFetcher;
import com.gmail.nossr50.api.ExperienceAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClaimLevels extends JavaPlugin
{


    public static YamlConfiguration LANG;
    public static File LANG_FILE;
    public static Logger log;
    DataManager dm = DataManager.getInstance();

    public static String getUUIDFromName(String name)
    {
        try
        {
            UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(name));
            Map<String, UUID> response = null;
            response = fetcher.call();
            return response.get(name).toString();
        } catch (Exception e)
        {
        }
        return null;
    }

    public static String getOfflinePlayerUUID(String name)
    {
        OfflinePlayer p = Bukkit.getServer().getOfflinePlayer(name);
        if (p.hasPlayedBefore())
        {
            return p.getUniqueId().toString();
        }
        return null;
    }

    public void onEnable()
    {
        PluginManager pm = getServer().getPluginManager();
        if (pm.isPluginEnabled("mcMMO") == false)
        {
            pm.disablePlugin(this);
            return;
        }
        pm.registerEvents(new PlayerListener(this), this);
        getConfig().options().copyDefaults(true);
        getConfig().addDefault("startupAmount", 0);
        saveConfig();
        loadLang();
        dm.setupData(this);
        convertToUUID();
    }

    public void convertToUUID()
    {
        if (getConfig().getBoolean("Converted") == false)
        {
            for (String name : dm.getData().getConfigurationSection("").getKeys(false))
            {
                String UUID = getUUIDFromName(name);
                if (UUID != null)
                {
                    int credits = dm.getData().getInt(name + ".credits");
                    dm.getData().set(UUID + ".credits", credits);
                    dm.getData().set(name, null);
                    dm.saveData();
                    getConfig().set("Converted", true);
                    saveConfig();
                    getLogger().info("Converted the player data to UUID");
                }
            }
        }
    }

    public void onDisable()
    {
        saveConfig();
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (label.equalsIgnoreCase("addcredits"))
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
                    if (target == null && offlineTarget.hasPlayedBefore() == true)
                    {
                        String targetPlayer = offlineTarget.getName().toLowerCase();
                        String UUID = getUUIDFromName(targetPlayer);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
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
                        int oldAmount = dm.getData().getInt(UUID + ".credits");
                        int newAmount = oldAmount + levels;
                        dm.getData().set(UUID + ".credits", newAmount);
                        dm.saveData();
                        sender.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have given " + targetPlayer + " " + ChatColor.GOLD + levels + ChatColor.GREEN + " credits");
                        return true;
                    }
                    else if (target == null && offlineTarget.hasPlayedBefore() == false)
                    {
                        sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER);
                    }
                    else if (target != null)
                    {
                        String targetPlayer = target.getName().toLowerCase();
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            sender.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int credits = Integer.parseInt(args[1]);
                        if (credits <= 0)
                        {
                            sender.sendMessage(Lang.PREFIX.toString() + Lang.POSITIVE_NUMBER);
                            return true;
                        }
                        if (!dm.getData().contains(target.getUniqueId().toString()))
                        {
                            dm.getData().set(target.getUniqueId().toString() + ".credits", dm.getData().getInt("startAmount"));
                        }
                        int oldAmount = dm.getData().getInt(target.getUniqueId().toString() + ".credits");
                        int newAmount = oldAmount + credits;
                        dm.getData().set(target.getUniqueId().toString() + ".credits", newAmount);
                        dm.saveData();
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
            else if (!(sender.hasPermission("credits.add")))
            {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMS);
            }
        }
        else if (label.equalsIgnoreCase("takecredits"))
        {
            if (sender.hasPermission("credits.take"))
            {
                if (args.length <= 1)
                {
                    sender.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "/takecredits <player> <credits>");
                }
                else if (args.length == 2)
                {
                    Player target = Bukkit.getServer().getPlayer(args[0]);
                    OfflinePlayer offlineTarget = Bukkit.getServer().getOfflinePlayer(args[0]);
                    if (target == null && offlineTarget.hasPlayedBefore() == true)
                    {
                        String targetPlayer = offlineTarget.getName().toLowerCase();
                        String UUID = getUUIDFromName(targetPlayer);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
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
                        int oldAmount = dm.getData().getInt(UUID + ".credits");
                        if (levels <= oldAmount)
                        {
                            int newAmount = oldAmount - levels;
                            dm.getData().set(UUID + ".credits", newAmount);
                            dm.saveData();
                            sender.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have taken " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels from " + targetPlayer);
                            return true;
                        }
                        else
                        {
                            sender.sendMessage(Lang.PREFIX.toString() + ChatColor.YELLOW + args[0] + ChatColor.GREEN + " already has " + ChatColor.GOLD + "0" + ChatColor.GREEN + " credits");
                            return true;
                        }
                    }
                    else if (target == null && offlineTarget.hasPlayedBefore() == false)
                    {
                        sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER);
                    }
                    else if (target != null)
                    {
                        String targetPlayer = target.getName().toLowerCase();
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            sender.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int credits = Integer.parseInt(args[1]);
                        if (credits <= 0)
                        {
                            sender.sendMessage(Lang.PREFIX.toString() + Lang.POSITIVE_NUMBER);
                            return true;
                        }
                        if (dm.getData().contains(target.getUniqueId().toString()))
                        {
                            int oldAmount = dm.getData().getInt(target.getUniqueId().toString() + ".credits");
                            if (credits <= oldAmount)
                            {
                                int newAmount = oldAmount - credits;
                                dm.getData().set(target.getUniqueId().toString() + ".credits", newAmount);
                                dm.saveData();
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
                else
                {
                    sender.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "/takecredits <player> <credits>");
                }
            }
            else if (!(sender.hasPermission("credits.take")))
            {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMS);
            }
        }
        else if (label.equalsIgnoreCase("credits"))
        {
            if (sender instanceof Player)
            {
                Player p = (Player) sender;
                if (args.length == 0)
                {
                    if (dm.getData().contains(p.getUniqueId().toString()))
                    {
                        int credits = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
                        p.sendMessage(Lang.CREDITPREFIX.toString() + ChatColor.GREEN + "You have " + ChatColor.GOLD + credits + ChatColor.GREEN + " credits");
                    }
                    else
                    {
                        p.sendMessage(Lang.CREDITPREFIX.toString() + ChatColor.GREEN + "You have " + ChatColor.GOLD + 0 + ChatColor.GREEN + " credits");
                    }
                }
                else if (args.length == 1)
                {
                    if (p.hasPermission("credits.inspect"))
                    {
                        String targetName = args[0].toString();
                        String UUID = getUUIDFromName(targetName);
                        if (UUID != null)
                        {
                            int credits = dm.getData().getInt(args[0].toLowerCase() + ".credits");
                            p.sendMessage(Lang.CREDITPREFIX.toString() + ChatColor.YELLOW + args[0].toLowerCase() + ChatColor.GREEN + " has " + ChatColor.GOLD + credits + ChatColor.GREEN + " credits");
                        }
                        else
                        {
                            p.sendMessage(Lang.CREDITPREFIX.toString() + ChatColor.YELLOW + args[0].toLowerCase() + ChatColor.GREEN + " has" + ChatColor.GOLD + " 0" + ChatColor.GREEN + " credits");
                        }
                    }
                    else if (!(p.hasPermission("credits.inspect")))
                    {
                        p.sendMessage(Lang.CREDITPREFIX.toString() + Lang.NO_PERMS);
                    }
                }
                else
                {
                    p.sendMessage(Lang.CREDITPREFIX.toString() + ChatColor.GREEN + "/credits <player>");
                }
            }
            else
            {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
            }
        }
        else if (label.equalsIgnoreCase("clreload"))
        {
            if (sender.hasPermission("claimlevels.reload"))
            {
                reloadConfig();
                dm.reloadData();
                sender.sendMessage(ChatColor.GREEN + "ClaimLevels config reloaded");
            }
            else if (!(sender.hasPermission("claimlevels.reload")))
            {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMS);
            }
        }
        else if (label.equalsIgnoreCase("redeem"))
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
                int cap = 0;
                if (skillType.equalsIgnoreCase("taming"))
                {
                    if (p.hasPermission("mcmmo.skills.taming"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int credits = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
                        if (oldamount < credits)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + ChatColor.RED + "You do not have enough credits!");
                            return true;
                        }
                        if (credits <= 0)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.POSITIVE_NUMBER);
                            return true;
                        }
                        if (ExperienceAPI.getLevel(p, skillType) + credits > cap)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + ChatColor.RED + "You have reached the maximum level for " + skillType);
                            return true;
                        }

                        int newamount = oldamount - credits;
                        if (newamount == 0)
                        {
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, credits);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + credits + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.taming")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("swords"))
                {
                    if (p.hasPermission("mcmmo.skills.swords"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.swords")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("unarmed"))
                {
                    if (p.hasPermission("mcmmo.skills.unarmed"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.unarmed")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("archery"))
                {
                    if (p.hasPermission("mcmmo.skills.archery"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.archery")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("axes"))
                {
                    if (p.hasPermission("mcmmo.skills.axes"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.axes")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("acrobatics"))
                {
                    if (p.hasPermission("mcmmo.skills.acrobatics"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.acrobatics")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("fishing"))
                {
                    if (p.hasPermission("mcmmo.skills.fishing"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.fishing")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("excavation"))
                {
                    if (p.hasPermission("mcmmo.skills.excavation"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.excavation")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("mining"))
                {
                    if (p.hasPermission("mcmmo.skills.mining"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.mining")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("herbalism"))
                {
                    ;
                    if (p.hasPermission("mcmmo.skills.herbalism"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.herbalism")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("repair"))
                {
                    if (p.hasPermission("mcmmo.skills.repair"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.repair")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("woodcutting"))
                {
                    if (p.hasPermission("mcmmo.skills.woodcutting"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.woodcutting")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (skillType.equalsIgnoreCase("alchemy"))
                {
                    if (p.hasPermission("mcmmo.skills.alchemy"))
                    {
                        cap = ExperienceAPI.getLevelCap(skillType);
                        try
                        {
                            Integer.parseInt(args[1]);
                        } catch (NumberFormatException ex)
                        {
                            p.sendMessage(Lang.PREFIX.toString() + Lang.MUST_BE_NUMBER);
                            return true;
                        }
                        int levels = Integer.parseInt(args[1]);
                        int oldamount = dm.getData().getInt(p.getUniqueId().toString() + ".credits");
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
                            dm.getData().set(p.getUniqueId().toString(), null);
                        }
                        else
                        {
                            dm.getData().set(p.getUniqueId().toString() + ".credits", newamount);
                        }
                        dm.saveData();
                        ExperienceAPI.addLevel(p, skillType, levels);
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.GREEN + "You have added " + ChatColor.GOLD + levels + ChatColor.GREEN + " levels to " + ChatColor.GOLD + skillType + ChatColor.GREEN + ".");
                        return true;
                    }
                    else if (!(p.hasPermission("mcmmo.skills.alchemy")))
                    {
                        p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "You do not have access to this skill");
                    }
                }
                else if (!(skillType.equalsIgnoreCase("taming") || skillType.equalsIgnoreCase("swords")
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
                }
            }
            else
            {
                p.sendMessage(Lang.PREFIX.toString() + ChatColor.DARK_RED + "/redeem <skill> <levels>");
            }
        }
        return false;
    }

    public void loadLang()
    {
        File lang = new File(getDataFolder(), "language.yml");
        OutputStream out = null;
        InputStream defLangStream = this.getResource("language.yml");
        if (!lang.exists())
        {
            try
            {
                getDataFolder().mkdir();
                lang.createNewFile();
                if (defLangStream != null)
                {
                    out = new FileOutputStream(lang);
                    int read = 0;
                    byte[] bytes = new byte[1024];

                    while ((read = defLangStream.read(bytes)) != -1)
                    {
                        out.write(bytes, 0, read);
                    }
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defLangStream);
                    Lang.setFile(defConfig);
                    return;
                }
            } catch (IOException e)
            {
                e.printStackTrace(); // So they notice
                log.severe("[ClaimLevels] Couldn't create language file.");
                log.severe("[ClaimLevels] This is a fatal error. Now disabling");
                this.setEnabled(false); // Without it loaded, we can't send them messages
            } finally
            {
                if (defLangStream != null)
                {
                    try
                    {
                        defLangStream.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (out != null)
                {
                    try
                    {
                        out.close();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }
            }
        }
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
        for (Lang item : Lang.values())
        {
            if (conf.getString(item.getPath()) == null)
            {
                conf.set(item.getPath(), item.getDefault());
            }
        }
        Lang.setFile(conf);
        ClaimLevels.LANG = conf;
        ClaimLevels.LANG_FILE = lang;
        try
        {
            conf.save(getLangFile());
        } catch (IOException e)
        {
            log.log(Level.WARNING, "ClaimLevels: Failed to save language.yml.");
            log.log(Level.WARNING, "ClaimLevels: Report this stack trace to xCyanide.");
            e.printStackTrace();
        }
    }

    public YamlConfiguration getLang()
    {
        return LANG;
    }

    public File getLangFile()
    {
        return LANG_FILE;
    }
}
