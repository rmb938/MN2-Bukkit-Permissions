package com.rmb938.bukkit.permissions.entity;

import java.util.ArrayList;
import java.util.HashMap;

public class Group {

    private static HashMap<String, Group> groups = new HashMap<>();

    public static HashMap<String, Group> getGroups() {
        return groups;
    }

    private int groupId;
    private String groupName;
    private int weight;
    private ArrayList<Group> inheritance = new ArrayList<>();
    private ArrayList<Permission> permissions = new ArrayList<>();

    public ArrayList<Group> getInheritance() {
        return inheritance;
    }

    public ArrayList<Permission> getPermissions() {
        return permissions;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
