package com.github.xCyanide;

import com.github.xCyanide.Utils.DataManager;
import com.github.xCyanide.Utils.Lang;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PlayerListener implements Listener
{

    private DataManager dm = DataManager.getInstance();
    private ClaimLevels plugin;

    public PlayerListener(ClaimLevels plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        Player player = event.getPlayer();
        if (!player.hasPlayedBefore())
        {
            int startupAmount = plugin.getConfig().getInt("startAmount");
            if (startupAmount > 0)
            {
                dm.getData().set(player.getUniqueId().toString() + ".credits", startupAmount);
                dm.saveData();
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        int levels = dm.getData().getInt(player.getUniqueId().toString() + ".credits");

        if (!dm.getData().contains(player.getUniqueId().toString()))
        {
            if(levels != 0)
                dm.getData().set(player.getUniqueId().toString() + ".credits", levels);
        }
    }
}