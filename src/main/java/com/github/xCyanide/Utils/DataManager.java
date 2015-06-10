package com.github.xCyanide.Utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class DataManager
{

    static DataManager instance = new DataManager();
    Plugin p;
    FileConfiguration data;
    File dfile;

    public static DataManager getInstance()
    {
        return instance;
    }

    public void setupData(Plugin p)
    {

        dfile = new File(p.getDataFolder(), "data.yml");

        if (!dfile.exists())
        {
            try
            {
                dfile.createNewFile();
            } catch (IOException e)
            {
                Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create data.yml!");
            }
        }
        data = YamlConfiguration.loadConfiguration(dfile);
    }

    public FileConfiguration getData()
    {
        return data;
    }

    public void saveData()
    {
        try
        {
            data.save(dfile);
        } catch (IOException e)
        {
            Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save data.yml!");
        }
    }

    public void reloadData()
    {
        data = YamlConfiguration.loadConfiguration(dfile);
    }
}

