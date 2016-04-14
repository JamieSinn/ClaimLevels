package com.github.xCyanide;

import com.github.xCyanide.Utils.DataManager;
import com.github.xCyanide.Utils.Lang;
import com.github.xCyanide.Utils.UUIDFetcher;
import com.github.xCyanide.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClaimLevels extends JavaPlugin
{
    private static ClaimLevels instance;
    private static YamlConfiguration LANG;
    private static File LANG_FILE;
    private Logger log = this.getLogger();
    public static DataManager dm = DataManager.getInstance();

    public static String getUUIDFromName(String name)
    {
        try
        {
            UUIDFetcher fetcher = new UUIDFetcher(Collections.singletonList(name));
            Map<String, UUID> response = null;
            response = fetcher.call();
            return response.get(name).toString();
        }
        catch (Exception ignored)
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
        instance = this;
        PluginManager pm = getServer().getPluginManager();
        if (!pm.isPluginEnabled("mcMMO"))
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

        getCommand("addcredits").setExecutor(new AddCredits());
        getCommand("credits").setExecutor(new Credits());
        getCommand("redeem").setExecutor(new Redeem());
        getCommand("clreload").setExecutor(new Reload());
        getCommand("takecredits").setExecutor(new TakeCredits());
    }

    public void convertToUUID()
    {
        if (!getConfig().getBoolean("Converted"))
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
            }
            catch (IOException e)
            {
                e.printStackTrace(); // So they notice
                log.severe("Couldn't create language file.");
                log.severe("This is a fatal error. Now disabling");
                this.setEnabled(false); // Without it loaded, we can't send them messages
            }
            finally
            {
                if (defLangStream != null)
                {
                    try
                    {
                        defLangStream.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (out != null)
                {
                    try
                    {
                        out.close();
                    }
                    catch (IOException e)
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
        }
        catch (IOException e)
        {
            log.log(Level.WARNING, "Failed to save language.yml.");
            log.log(Level.WARNING, "Report this stack trace to JamieSinn.");
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

    public static ClaimLevels getInstance()
    {
        return instance;
    }
}
