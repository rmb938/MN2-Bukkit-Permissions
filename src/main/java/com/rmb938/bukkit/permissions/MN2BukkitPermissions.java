package com.rmb938.bukkit.permissions;

import com.rmb938.bukkit.permissions.config.MainConfig;
import com.rmb938.bukkit.permissions.database.PermissionInfoLoader;
import com.rmb938.bukkit.permissions.jedis.NetCommandHandlerBTS;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MN2BukkitPermissions extends JavaPlugin {

    private MainConfig mainConfig;
    private PermissionInfoLoader permissionInfoLoader;

    public void onEnable() {
        mainConfig = new MainConfig(this);
        try {
            mainConfig.init();
            mainConfig.save();
        } catch (net.cubespace.Yamler.Config.InvalidConfigurationException e) {
            getLogger().log(Level.SEVERE, null, e);
            return;
        }
        permissionInfoLoader = new PermissionInfoLoader(this);
        new NetCommandHandlerBTS(this);
    }

    public void onDisable() {

    }

    public PermissionInfoLoader getPermissionInfoLoader() {
        return permissionInfoLoader;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }
}
