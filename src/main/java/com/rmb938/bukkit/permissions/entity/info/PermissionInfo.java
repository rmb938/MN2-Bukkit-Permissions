package com.rmb938.bukkit.permissions.entity.info;

import com.rmb938.bukkit.base.entity.info.UserInfo;
import org.bukkit.permissions.PermissionAttachment;

public class PermissionInfo extends UserInfo {

    private final PermissionAttachment permissionAttachment;

    public PermissionInfo(PermissionAttachment permissionAttachment) {
        super(PermissionInfo.class.getName());
        this.permissionAttachment = permissionAttachment;
    }

    public PermissionAttachment getPermissionAttachment() {
        return permissionAttachment;
    }
}
