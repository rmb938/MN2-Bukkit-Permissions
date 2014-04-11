package com.rmb938.bukkit.permissions.database;

import com.rmb938.bukkit.base.database.UserInfoLoader;
import com.rmb938.bukkit.base.entity.User;
import com.rmb938.bukkit.permissions.entity.info.PermissionInfo;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class PermissionInfoLoader extends UserInfoLoader<PermissionInfo> {

    public PermissionInfoLoader() {
        super(PermissionInfo.class);
    }

    @Override
    public void createTable() {

    }

    @Override
    public PermissionInfo loadUserInfo(User user) {
        PermissionInfo permissionInfo = null;



        return permissionInfo;
    }

    @Override
    public void createUserInfo(User user) {

    }

    @Override
    public void saveUserInfo(User user) {

    }

    @Override
    public void createTempUserInfo(User user) {
        Player player = Bukkit.getPlayer(user.getUserUUID());
        Bukkit.getLogger().info("Adding Permission Info to user "+player.getName());
        PermissionAttachment permissionAttachment = player.addAttachment(Bukkit.getPluginManager().getPlugin("MN2BukkitPermissions"));
        PermissionInfo permissionInfo = new PermissionInfo(permissionAttachment);
        user.getUserInfo().put(permissionInfo.getClass(), permissionInfo);
    }
}
