package com.github.xCyanide.Utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Lang
{
    PREFIX("prefix", "&8[&3ClaimLevels&8]"),
    CREDITPREFIX("credit-prefix", "&8[&3LevelBalance&8]"),
    PLAYER_ONLY("player-only", "Sorry but that can only be run by a player!"),
    MUST_BE_NUMBER("must-be-number", "&4The amount must be a number"),
    POSITIVE_NUMBER("positive-number", "&4The amount must be a positive number"),
    NO_PERMS("no-permissions", "&4You don't have permission for that!"),
    PLAYER("player-exist", "&4That player does not exist");

    private static YamlConfiguration LANG;
    private String path;
    private String def;

    /**
     * Lang enum constructor.
     *
     * @param path  The string path.
     * @param start The default string.
     */
    Lang(String path, String start)
    {
        this.path = path;
        this.def = start;
    }

    /**
     * Set the {@code YamlConfiguration} to use.
     *
     * @param config The config to set.
     */
    public static void setFile(YamlConfiguration config)
    {
        LANG = config;
    }

    @Override
    public String toString()
    {
        if (this == PREFIX)
            return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def)) + " ";
        return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def));
    }

    /**
     * Get the default value of the path.
     *
     * @return The default value of the path.
     */
    public String getDefault()
    {
        return this.def;
    }

    /**
     * Get the path to the string.
     *
     * @return The path to the string.
     */
    public String getPath()
    {
        return this.path;
    }
}
