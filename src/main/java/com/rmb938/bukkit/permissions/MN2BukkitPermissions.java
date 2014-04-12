package com.rmb938.bukkit.permissions;

import com.rmb938.bukkit.permissions.config.MainConfig;
import com.rmb938.bukkit.permissions.database.PermissionInfoLoader;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MN2BukkitPermissions extends JavaPlugin {

    private MainConfig mainConfig;

    public void onEnable() {
        mainConfig = new MainConfig(this);
        try {
            mainConfig.init();
            mainConfig.save();
        } catch (net.cubespace.Yamler.Config.InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, null, e);
            return;
        }
        new PermissionInfoLoader(this);
    }

    public void onDisable() {

    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }
}
