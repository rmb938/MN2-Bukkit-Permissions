package com.rmb938.bukkit.permissions;

import com.rmb938.bukkit.permissions.database.PermissionInfoLoader;
import com.rmb938.bukkit.permissions.jedis.NetCommandHandlerBTS;
import org.bukkit.plugin.java.JavaPlugin;

public class MN2BukkitPermissions extends JavaPlugin {

    private PermissionInfoLoader permissionInfoLoader;

    public void onEnable() {
        permissionInfoLoader = new PermissionInfoLoader(this);
        new NetCommandHandlerBTS(this);
    }

    public void onDisable() {

    }

    public PermissionInfoLoader getPermissionInfoLoader() {
        return permissionInfoLoader;
    }
}
