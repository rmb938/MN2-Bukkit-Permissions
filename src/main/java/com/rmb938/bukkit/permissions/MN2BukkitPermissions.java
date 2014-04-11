package com.rmb938.bukkit.permissions;

import com.rmb938.bukkit.permissions.database.PermissionInfoLoader;
import org.bukkit.plugin.java.JavaPlugin;

public class MN2BukkitPermissions extends JavaPlugin {

    public void onEnable() {
        new PermissionInfoLoader();
    }

    public void onDisable() {

    }

}
