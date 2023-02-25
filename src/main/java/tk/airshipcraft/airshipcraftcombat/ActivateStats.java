package tk.airshipcraft.airshipcraftcombat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import tk.airshipcraft.airshipcraftcombat.DamageListener.PlayerStatsManager;

import java.util.Map;

public class ActivateStats implements Listener {
    private PlayerStatsManager statsManager;
    private Map<LivingEntity, PlayerStats> entityStats;
    private ActivateStats instance = null;

    public ActivateStats(PlayerStatsManager statsManager) {
        this.statsManager = statsManager;
        this.entityStats = statsManager.entityStats;
    }

    public PlayerStatsManager getStatsManager() {
        return statsManager;
    }



    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = new PlayerStats(100, 10, 5, 2, 5, 1, 100);
        entityStats.put(player, stats);
        System.out.println(entityStats);
        System.out.println(player + "'s stats have successfully loaded");

    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            if (event.getEntity() instanceof ArmorStand) {
                if (event.getEntity() instanceof Player) {
                } else {
                    LivingEntity entity = (LivingEntity) event.getEntity();
                    PlayerStats stats = new PlayerStats(100, 10, 5, 2, 5, 1, 100);
                    entityStats.put(entity, stats);
                    for (LivingEntity entity2 : entityStats.keySet()) {
                        PlayerStats stats2 = entityStats.get(entity2);
                        double health = stats2.getHealth(entity);
                        double maxHealth = stats2.getMaxHealth(entity);
                        double percentage = health / maxHealth * 100.0;
                        String message = String.format(ChatColor.RED + "Health: %.1f / %.1f (%.0f%%)", health, maxHealth, percentage);
                        entity2.setCustomName(message);
                        entity2.setCustomNameVisible(true);
                    }
                }
            }
        }
    }
}
