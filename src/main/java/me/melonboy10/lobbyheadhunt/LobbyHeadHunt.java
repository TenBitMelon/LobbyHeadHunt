package me.melonboy10.lobbyheadhunt;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public final class LobbyHeadHunt extends JavaPlugin implements Listener {

    World hubWorld;
    ArrayList<Skull> skulls = new ArrayList<>();
    HashMap<Player, ArrayList<Skull>> playerSkulls = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        hubWorld = Bukkit.getWorld("world");

        for (Chunk chunk : hubWorld.getLoadedChunks()) {
            for (BlockState tileEntity : chunk.getTileEntities()) {
                if (tileEntity instanceof Skull skull) {
                    skulls.add(skull);
                    System.out.println("add skulls");
                    if (skull.getPersistentDataContainer().has(new NamespacedKey(this, "players"), PersistentDataType.STRING)) {
                        System.out.println("had players");
                        for (String s : skull.getPersistentDataContainer().get(new NamespacedKey(this, "players"), PersistentDataType.STRING).split(",")) {
                            System.out.println("player: " + s);
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(s);

                            if (!playerSkulls.containsKey(offlinePlayer.getPlayer())) {
                                playerSkulls.put(offlinePlayer.getPlayer(), new ArrayList<>());
                            }
                            playerSkulls.get(offlinePlayer.getPlayer()).add(skull);
                        }
                    } else {
                        skull.getPersistentDataContainer().set(new NamespacedKey(this, "players"), PersistentDataType.STRING, "");
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {

    }


    @EventHandler
    public void onClickHead(PlayerInteractEvent event) {
        if (event.getHand().equals(EquipmentSlot.HAND)) {
            if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                System.out.println(event.getClickedBlock().getType());
                if (event.getClickedBlock().getType().equals(Material.PLAYER_HEAD)) {
                    Skull skull = (Skull) event.getClickedBlock().getState();
                    Player player = event.getPlayer();
                    if (!skull.getPersistentDataContainer().has(new NamespacedKey(this, "players"), PersistentDataType.STRING)) {
                        skull.getPersistentDataContainer().set(new NamespacedKey(this, "players"), PersistentDataType.STRING, "");
                    }
                    if (skull.getPersistentDataContainer().get(new NamespacedKey(this, "players"), PersistentDataType.STRING).contains(player.getName())) {
                        player.sendMessage(ChatColor.RED + "You have already claimed this present!");
                    } else {
                        if (!playerSkulls.containsKey(player)) playerSkulls.put(player, new ArrayList<>());
                        playerSkulls.get(player).add(skull);
                        player.sendMessage(ChatColor.GREEN + "You found a present! " +
                            ChatColor.YELLOW + "(" +
                            ChatColor.RED + playerSkulls.get(player).size() +
                            ChatColor.YELLOW + "/" +
                            ChatColor.RED + skulls.size() +
                            ChatColor.YELLOW + ")");
                        skull.getPersistentDataContainer().set(new NamespacedKey(this, "players"), PersistentDataType.STRING,
                            skull.getPersistentDataContainer().get(new NamespacedKey(this, "players"), PersistentDataType.STRING) + player.getName());
                        skull.update();
                    }
                }
            }
        }
    }
}
