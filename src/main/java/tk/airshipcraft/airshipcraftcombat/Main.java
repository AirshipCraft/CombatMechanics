package tk.airshipcraft.airshipcraftcombat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import tk.airshipcraft.airshipcraftcombat.DamageListener.PlayerStatsManager;

import java.util.Map;

public final class Main extends JavaPlugin {
    private Map<LivingEntity, PlayerStats> entityStats;
    PlayerStatsManager statsManager = new PlayerStatsManager();
    ActivateStats activateStats = new ActivateStats(statsManager);
    public Main() {
        this.entityStats = statsManager.entityStats;
    }
    private final BukkitRunnable actionBarTask = new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerStats playerStats = entityStats.get(player);
                double health = playerStats.getHealth(player);
                double maxHealth = playerStats.getMaxHealth(player);
                double percentage = health / maxHealth * 100.0;
                String message = String.format("Health: %.1f / %.1f (%.0f%%)", health, maxHealth, percentage);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            }
        }
    };
    @Override
    public void onEnable() {
        DamageListener damageListener = new DamageListener(this, activateStats);
        actionBarTask.runTaskTimer(this, 0L, 20L);
        getServer().getPluginManager().registerEvents(damageListener, this);
        getServer().getPluginManager().registerEvents(new ActivateStats(statsManager), this);
    }



    @Override
    public void onDisable() {

    }
}

