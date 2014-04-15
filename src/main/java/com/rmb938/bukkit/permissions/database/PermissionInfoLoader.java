package com.rmb938.bukkit.permissions.database;

import com.mongodb.*;
import com.rmb938.bukkit.base.database.UserInfoLoader;
import com.rmb938.bukkit.base.entity.User;
import com.rmb938.bukkit.permissions.MN2BukkitPermissions;
import com.rmb938.bukkit.permissions.entity.Group;
import com.rmb938.bukkit.permissions.entity.Permission;
import com.rmb938.bukkit.permissions.entity.info.PermissionInfo;
import com.rmb938.database.DatabaseAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PermissionInfoLoader extends UserInfoLoader<PermissionInfo> {

    private final MN2BukkitPermissions plugin;

    public PermissionInfoLoader(MN2BukkitPermissions plugin) {
        super(PermissionInfo.class);
        this.plugin = plugin;
        loadGroups();
    }

    @Override
    public void createTable() {
        if (DatabaseAPI.getMongoDatabase().collectionExists("mn2_permissions_groups") == false) {
            DatabaseAPI.getMongoDatabase().createCollection("mn2_permissions_groups");
        }
    }

    public void loadGroups() {
        Group.getGroups().clear();
        Map.Entry<DBCursor, MongoClient> dbCursorMongoClientEntry = DatabaseAPI.getMongoDatabase().findMany("mn2_permissions_groups");
        MongoClient mongoClient = dbCursorMongoClientEntry.getValue();
        DBCursor dbCursor = dbCursorMongoClientEntry.getKey();

        while (dbCursor.hasNext()) {
            DBObject dbObject = dbCursor.next();
            String groupName = (String) dbObject.get("groupName");
            loadGroup(groupName);
        }
        dbCursor.close();

        DatabaseAPI.getMongoDatabase().returnClient(mongoClient);

        if (Group.getGroups().containsKey(plugin.getMainConfig().defaultGroup) == false) {
            createGroup(plugin.getMainConfig().defaultGroup, 0);
        }
    }

    public void loadGroup(String groupName) {
        DBObject dbObject = DatabaseAPI.getMongoDatabase().findOne("mn2_permissions_groups", new BasicDBObject("groupName", groupName));
        if (dbObject == null) {
            plugin.getLogger().warning("Unknown Group " + groupName);
            return;
        }
        int weight = (Integer) dbObject.get("weight");
        Group group = new Group();
        group.setGroupName(groupName);
        group.setWeight(weight);

        BasicDBList inheritance = (BasicDBList) dbObject.get("inheritance");
        for (Object anInheritance : inheritance) {
            String groupName1 = (String) anInheritance;
            if (Group.getGroups().containsKey(groupName1)) {
                group.getInheritance().add(Group.getGroups().get(groupName1));
            } else {
                loadGroup(groupName1);
                if (Group.getGroups().containsKey(groupName1)) {
                    group.getInheritance().add(Group.getGroups().get(groupName1));
                } else {
                    plugin.getLogger().warning("Unknown group adding to group " + groupName + " removing");
                    DatabaseAPI.getMongoDatabase().updateDocument("mn2_permission_groups", new BasicDBObject("groupName", group.getGroupName()),
                            new BasicDBObject("$pull", new BasicDBObject("inheritance", groupName)));
                }
            }
        }

        BasicDBList permissions = (BasicDBList) dbObject.get("permissions");
        for (Object permission1 : permissions) {
            BasicDBObject dbObject1 = (BasicDBObject) permission1;
            String permissionString = (String) dbObject1.get("permission");
            String serverType = (String) dbObject1.get("serverType");
            Permission permission = new Permission();
            permission.setPermission(permissionString);
            permission.setServerType(serverType);
            group.getPermissions().add(permission);
        }
        Group.getGroups().put(groupName, group);
    }

    public void createGroup(String groupName, int weight) {
        BasicDBObject groupObject = new BasicDBObject("groupName", groupName);
        groupObject.append("weight", weight);
        groupObject.append("inheritance", new BasicDBList());
        groupObject.append("permissions", new BasicDBList());

        DatabaseAPI.getMongoDatabase().insert("mn2_permissions_groups", groupObject);

        loadGroup(groupName);
    }

    private void addInheritance(Group group, org.bukkit.permissions.Permission perm) {
        for (Group group1 : group.getInheritance()) {
            addInheritance(group1, perm);
        }
        for (Permission permission : group.getPermissions()) {
            if (permission.getServerType().equals("bungee")) {
                continue;
            }
            if (permission.getServerType().equals("global") == false) {
                if (permission.getServerType().equals(plugin.getServer().getServerName().split("\\.")[0]) == false) {
                    continue;
                }
            }
            if (permission.getPermission().startsWith("-")) {
                perm.getChildren().put(permission.getPermission().substring(1, permission.getPermission().length()), false);
                //permissionAttachment.setPermission(permission.getPermission().substring(1, permission.getPermission().length()), false);
            } else {
                perm.getChildren().put(permission.getPermission(), true);
                //permissionAttachment.setPermission(permission.getPermission(), true);
            }
        }
    }

    @Override
    public PermissionInfo loadUserInfo(User user, Player player) {
        if (player == null) {
            return null;
        }
        DBObject userObject = DatabaseAPI.getMongoDatabase().findOne("mn2_users", new BasicDBObject("userUUID", user.getUserUUID()));
        if (userObject == null) {
            plugin.getLogger().warning("Unknown user permission info " + player.getName());
            return null;
        }
        plugin.getLogger().info(userObject.toString());
        PermissionInfo permissionInfo;
        org.bukkit.permissions.Permission perm;
        if (user.getUserInfo().containsKey(PermissionInfo.class)) {
            permissionInfo = (PermissionInfo) user.getUserInfo().get(PermissionInfo.class);
            permissionInfo.getGroups().clear();
            permissionInfo.getPermissions().clear();
            perm = Bukkit.getPluginManager().getPermission("mn2.permissions."+user.getUserUUID());
            perm.getChildren().clear();
            perm.recalculatePermissibles();
        } else {
            perm = new org.bukkit.permissions.Permission("mn2.permissions."+user.getUserUUID(), PermissionDefault.FALSE, new HashMap<String, Boolean>());
            Bukkit.getPluginManager().addPermission(perm);
            player.addAttachment(plugin, perm.getName(), true);
            permissionInfo = new PermissionInfo();
        }
        if (userObject.containsField("groups") == false) {
            return null;
        }

        BasicDBList groupsList = (BasicDBList) userObject.get("groups");
        for (Object aGroupsList : groupsList) {
            String groupName = (String) aGroupsList;
            if (Group.getGroups().containsKey(groupName)) {
                permissionInfo.getGroups().add(Group.getGroups().get(groupName));
            } else {
                plugin.getLogger().warning("Unknown group adding to user " + groupName + " removing.");
                DatabaseAPI.getMongoDatabase().updateDocument("mn2_users", new BasicDBObject("userUUID", user.getUserUUID()),
                        new BasicDBObject("$pull", new BasicDBObject("groups", groupName)));
            }
        }

        if (permissionInfo.getGroups().size() == 0) {
            permissionInfo.getGroups().add(Group.getGroups().get(plugin.getMainConfig().defaultGroup));
            DatabaseAPI.getMongoDatabase().updateDocument("mn2_users", new BasicDBObject("userUUID", user.getUserUUID()),
                    new BasicDBObject("$push", new BasicDBObject("groups",plugin.getMainConfig().defaultGroup)));
        } else {
            Collections.sort(permissionInfo.getGroups(), new Comparator<Group>() {
                @Override
                public int compare(Group o1, Group o2) {
                    if (o1.getWeight() > o2.getWeight()) {
                        return 1;
                    }
                    if (o1.getWeight() < o2.getWeight()) {
                        return -1;
                    }
                    return 0;
                }
            });
        }

        addInheritance(permissionInfo.getGroups().get(0), perm);

        BasicDBList permissionsList = (BasicDBList) userObject.get("permissions");
        for (Object aPermissionsList : permissionsList) {
            DBObject permissionObject = (DBObject) aPermissionsList;
            String permissionString = (String) permissionObject.get("permission");
            String serverType = (String) permissionObject.get("serverType");
            Permission permission = new Permission();
            permission.setPermission(permissionString);
            permission.setServerType(serverType);
            permissionInfo.getPermissions().add(permission);
        }

        for (Permission permission : permissionInfo.getPermissions()) {
            if (permission.getServerType().equalsIgnoreCase("global") == true) {
                if (permission.getPermission().startsWith("-")) {
                    perm.getChildren().put(permission.getPermission().substring(1, permission.getPermission().length()), false);
                    //permissionAttachment.setPermission(permission.getPermission().substring(1, permission.getPermission().length()), false);
                } else {
                    perm.getChildren().put(permission.getPermission(), true);
                    //permissionAttachment.setPermission(permission.getPermission(), true);
                }
            }
        }

        for (Permission permission : permissionInfo.getPermissions()) {
            if (permission.getServerType().equalsIgnoreCase(plugin.getServer().getServerName().split("\\.")[0]) == true) {
                if (permission.getPermission().startsWith("-")) {
                    perm.getChildren().put(permission.getPermission().substring(1, permission.getPermission().length()), false);
                    //permissionAttachment.setPermission(permission.getPermission().substring(1, permission.getPermission().length()), false);
                } else {
                    perm.getChildren().put(permission.getPermission(), true);
                    //permissionAttachment.setPermission(permission.getPermission(), true);
                }
            }
        }
        perm.recalculatePermissibles();
        user.getUserInfo().put(PermissionInfo.class, permissionInfo);
        return permissionInfo;
    }

    @Override
    public void createUserInfo(User user, Player player) {
        if (player == null) {
            return;
        }
        DatabaseAPI.getMongoDatabase().updateDocument("mn2_users", new BasicDBObject("userUUID", user.getUserUUID()),
                new BasicDBObject("$set", new BasicDBObject("groups", new BasicDBList())));
        DatabaseAPI.getMongoDatabase().updateDocument("mn2_users", new BasicDBObject("userUUID", user.getUserUUID()),
                new BasicDBObject("$set", new BasicDBObject("permissions", new BasicDBList())));
    }

    @Override
    public void saveUserInfo(User user, Player player, boolean remove) {
        if (remove == true) {
            Bukkit.getPluginManager().removePermission("mn2.permissions." + user.getUserUUID());
        }
    }
}
