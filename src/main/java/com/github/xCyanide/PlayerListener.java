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

    DataManager dm = DataManager.getInstance();
    @SuppressWarnings("unused")
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
            int startupAmount = dm.getData().getInt("startAmount");
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

        player.sendMessage(Lang.PREFIX.toString() + ChatColor.AQUA + "You have "
                + ChatColor.DARK_AQUA + (levels > 0 ? levels : 0)
                + ChatColor.AQUA + " level(s) to redeem on a mcMMO skill");
        if (!dm.getData().contains(player.getUniqueId().toString()))
        {
            dm.getData().set(player.getUniqueId().toString() + ".credits", levels);
        }

    }
}