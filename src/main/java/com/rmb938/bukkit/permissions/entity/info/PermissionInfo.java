package com.rmb938.bukkit.permissions.entity.info;

import com.rmb938.bukkit.base.entity.info.UserInfo;
import com.rmb938.bukkit.permissions.entity.Group;
import com.rmb938.bukkit.permissions.entity.Permission;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;

public class PermissionInfo extends UserInfo {

    private final PermissionAttachment permissionAttachment;

    private ArrayList<Group> groups = new ArrayList<>();
    private ArrayList<Permission> permissions = new ArrayList<>();

    public PermissionInfo(PermissionAttachment permissionAttachment) {
        super(PermissionInfo.class.getName());
        this.permissionAttachment = permissionAttachment;
    }

    public ArrayList<Group> getGroups() {
        return groups;
    }

    public ArrayList<Permission> getPermissions() {
        return permissions;
    }

    public PermissionAttachment getPermissionAttachment() {
        return permissionAttachment;
    }
}
