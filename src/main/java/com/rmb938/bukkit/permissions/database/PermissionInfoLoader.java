package com.rmb938.bukkit.permissions.database;

import com.rmb938.bukkit.base.database.UserInfoLoader;
import com.rmb938.bukkit.base.entity.User;
import com.rmb938.bukkit.permissions.entity.Group;
import com.rmb938.bukkit.permissions.entity.Permission;
import com.rmb938.bukkit.permissions.entity.info.PermissionInfo;
import com.rmb938.database.DatabaseAPI;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class PermissionInfoLoader extends UserInfoLoader<PermissionInfo> {

    public PermissionInfoLoader() {
        super(PermissionInfo.class);
        loadGroups();
    }

    @Override
    public void createTable() {

    }

    private void loadGroups() {
        ArrayList<Object> objects = DatabaseAPI.getMySQLDatabase().getBeansInfo("select groupName from `mn2_permission_groups`", new MapListHandler());
        for (Object obj : objects) {
            Map map = (Map) obj;
            String groupName = (String) map.get("groupName");
            loadGroup(groupName);
        }
    }

    private void loadGroup(String groupName) {
        ArrayList<Object> objects = DatabaseAPI.getMySQLDatabase().getBeansInfo("select groupId, groupName, weight from `mn2_permission_groups` where groupName='" + groupName + "'", new BeanListHandler<>(Group.class));
        for (Object obj : objects) {
            Group group = (Group) obj;

            ArrayList<Object> objects1 = DatabaseAPI.getMySQLDatabase().getBeansInfo("select groupName from `mn2_permission_members` join `mn2_permission_entities` ON memberOf = mn2_permission_entities.entityId join `mn2_permission_groups` ON mn2_permission_entities.entityLink = groupId where mn2_permission_members.entityId= (select entityId from `mn2_permission_entities` join `mn2_permission_groups` ON entityLink = groupId where groupName='" + group.getGroupName() + "')", new MapListHandler());
            for (Object obj1 : objects1) {
                Map map = (Map) obj1;
                String groupName1 = (String) map.get("groupName");
                if (Group.getGroups().containsKey(groupName1) == false) {
                    loadGroup(groupName1);
                } else {
                    group.getInheritance().add(Group.getGroups().get(groupName1));
                }
            }

            objects1 = DatabaseAPI.getMySQLDatabase().getBeansInfo("select permission, serverType from `mn2_permission_entries` where entityId = (select entityId from `mn2_permission_entities` join `mn2_permission_groups` ON entityLink = groupId where groupName='" + group.getGroupName() + "')", new BeanListHandler<>(Permission.class));
            for (Object obj1 : objects1) {
                Permission permission = (Permission) obj1;
                group.getPermissions().add(permission);
            }

            Group.getGroups().put(group.getGroupName(), group);
        }
    }

    @Override
    public PermissionInfo loadUserInfo(User user) {
        Player player = Bukkit.getPlayer(user.getUserUUID());
        Bukkit.getLogger().info("Adding Permission Info to user " + player.getName());
        PermissionAttachment permissionAttachment = player.addAttachment(Bukkit.getPluginManager().getPlugin("MN2BukkitPermissions"));
        PermissionInfo permissionInfo = new PermissionInfo(permissionAttachment);

        ArrayList<Object> objects = DatabaseAPI.getMySQLDatabase().getBeansInfo("select groupName from `mn2_permission_memberships` join `mn2_permission_entities` on memberOf = mn2_permission_entities.entityId join `mn2_permission_groups` on entityLink = groupId where mn2_permission_memberships.entityId = (select entityId from `mn2_permission_entities` join `mn2_users` ON entityLink = userUUID where userUUID='" + user.getUserUUID() + "')", new MapListHandler());
        for (Object obj : objects) {
            Map map = (Map) obj;
            String groupName = (String) map.get("groupName");
            Group group = Group.getGroups().get(groupName);
            permissionInfo.getGroups().add(group);
        }

        Collections.sort(permissionInfo.getGroups(), new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                if (o1.getWeight() > o2.getWeight()) {
                    return -1;
                }
                if (o1.getWeight() < o2.getWeight()) {
                    return 1;
                }
                return  0;
            }
        });

        for (Group group : permissionInfo.getGroups()) {
            for (Group group1 : group.getInheritance()) {
                for (Permission permission : group1.getPermissions()) {
                    if (permission.getPermission().startsWith("-")) {
                        permissionAttachment.setPermission(permission.getPermission().substring(1, permission.getPermission().length()), false);
                    } else {
                        permissionAttachment.setPermission(permission.getPermission(), true);
                    }
                }
            }
            for (Permission permission : group.getPermissions()) {
                if (permission.getPermission().startsWith("-")) {
                    permissionAttachment.setPermission(permission.getPermission().substring(1, permission.getPermission().length()), false);
                } else {
                    permissionAttachment.setPermission(permission.getPermission(), true);
                }
            }
        }

        objects = DatabaseAPI.getMySQLDatabase().getBeansInfo("select permission, serverType from `mn2_permission_entries` join `mn2_permission_entities` using(entityId) where entityLink='"+user.getUserUUID()+"'", new BeanListHandler<>(Permission.class));
        for (Object obj : objects) {
            Permission permission = (Permission) obj;
            permissionInfo.getPermissions().add(permission);

            if (permission.getPermission().startsWith("-")) {
                permissionAttachment.setPermission(permission.getPermission().substring(1, permission.getPermission().length()), false);
            } else {
                permissionAttachment.setPermission(permission.getPermission(), true);
            }
        }

        user.getUserInfo().put(permissionInfo.getClass(), permissionInfo);
        return permissionInfo;
    }

    @Override
    public void createUserInfo(User user) {
        Player player = Bukkit.getPlayer(user.getUserUUID());
        Bukkit.getLogger().info("Creating Permission Info for user "+player.getName());
        DatabaseAPI.getMySQLDatabase().updateQueryPS("INSERT INTO `mn2_permission_entities` (entityId, type, entityLink) VALUES (NULL, ?, ?)", "user", user.getUserUUID());
    }

    @Override
    public void saveUserInfo(User user) {

    }

    @Override
    public void createTempUserInfo(User user) {
        Player player = Bukkit.getPlayer(user.getUserUUID());
        Bukkit.getLogger().info("Adding Temp Permission Info to user " + player.getName());
        PermissionAttachment permissionAttachment = player.addAttachment(Bukkit.getPluginManager().getPlugin("MN2BukkitPermissions"));
        PermissionInfo permissionInfo = new PermissionInfo(permissionAttachment);
        user.getUserInfo().put(permissionInfo.getClass(), permissionInfo);
    }
}
