package com.rmb938.bukkit.permissions.jedis;

import com.rmb938.bukkit.base.MN2BukkitBase;
import com.rmb938.bukkit.base.entity.User;
import com.rmb938.bukkit.permissions.MN2BukkitPermissions;
import com.rmb938.bukkit.permissions.entity.info.PermissionInfo;
import com.rmb938.jedis.net.NetChannel;
import com.rmb938.jedis.net.NetCommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class NetCommandHandlerBTS  extends NetCommandHandler {

    private final MN2BukkitPermissions plugin;
    private final MN2BukkitBase basePlugin;

    public NetCommandHandlerBTS(MN2BukkitPermissions plugin) {
        NetCommandHandler.addHandler(NetChannel.BUNGEE_TO_SERVER, this);
        this.plugin = plugin;
        this.basePlugin = (MN2BukkitBase) plugin.getServer().getPluginManager().getPlugin("MN2BukkitBase");
    }

    @Override
    public void handle(JSONObject jsonObject) {
        try {
            String fromBungee = jsonObject.getString("from");
            String toServer = jsonObject.getString("to");

            if (toServer.equalsIgnoreCase("*") == false) {
                if (toServer.equalsIgnoreCase(basePlugin.getServerUUID()) == false) {
                    return;
                }
            }

            String command = jsonObject.getString("command");
            HashMap<String, Object> objectHashMap = objectToHashMap(jsonObject.getJSONObject("data"));
            switch (command) {
                case "reloadGroups":
                    plugin.getPermissionInfoLoader().loadGroups();
                    plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            for (Player player : plugin.getServer().getOnlinePlayers()) {
                                User user = basePlugin.getUserLoader().getUser(player);
                                if (user.getUserInfo().containsKey(PermissionInfo.class)) {
                                    plugin.getPermissionInfoLoader().loadUserInfo(user, player);
                                }
                            }
                        }
                    });
                    break;
                case "reloadUser":
                    String playerUUID = (String) objectHashMap.get("playerUUID");
                    Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
                    if (player == null) {
                        return;
                    }
                    plugin.getPermissionInfoLoader().loadUserInfo(basePlugin.getUserLoader().getUser(player), player);
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            plugin.getLogger().log(Level.SEVERE, null, e);
        }
    }
}
