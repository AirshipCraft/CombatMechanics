package tk.airshipcraft.airshipcraftcombat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class DamageListener implements Listener {
    private final Plugin plugin;
    private ActivateStats activateStats;

    public static class PlayerStatsManager {
        public Map<LivingEntity, PlayerStats> entityStats = new HashMap<>();
    }
    PlayerStatsManager statsManager;
    Map<LivingEntity, PlayerStats> entityStats;

    public DamageListener(Plugin plugin, ActivateStats activateStats){
        this.plugin = plugin;
        this.activateStats = activateStats;
        this.entityStats = activateStats.getStatsManager().entityStats;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) event.getEntity();
        double health = entity.getHealth();


        PlayerStats victimStats = entityStats.get(entity);
        PlayerStats damagerStats = null;
        double damageTaken = entity.getLastDamage();
        Entity damager = null;
        ItemStack weapon = null;
        ItemStack[] armor = null;

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Player) {
                damagerStats = entityStats.get(damager);
                Player playerDamager = (Player) damager;
                weapon = playerDamager.getInventory().getItemInMainHand();
                armor = playerDamager.getInventory().getArmorContents();
            } else {
                damagerStats = entityStats.get(damager);
            }
        }
        UUID uuid = entity.getUniqueId();
        double customHealth = victimStats.getHealth(entity);
        double maxHealth = victimStats.getMaxHealth(entity);

        try {
            double damage = calculateDamage(damagerStats, weapon, armor, entity);
            dealCustomDamage(damage, entity);
        } catch(Exception e) {
            throw new RuntimeException(damager + "'s stats don't exist, please tell him to relog");
        }

        if (customHealth <= 0) {
            entity.setHealth(0);
        }

        if (damageTaken >= health) {
            event.setCancelled(true);
        }

        for (LivingEntity entityKey : entityStats.keySet()) {
            double percentage = customHealth / maxHealth * 100.0;
            String message = String.format(ChatColor.RED + "Health: %.1f / %.1f (%.0f%%)", customHealth, maxHealth, percentage);
            entityKey.setCustomName(message);
            entityKey.setCustomNameVisible(true);
        }
    }
    public double calculateDamage(PlayerStats stats, ItemStack weapon, ItemStack[] armor, LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        double baseDamage = stats.getBaseDamage(entity);
        if (weapon != null && weapon.getType() != Material.AIR) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta.hasLore()) {
                List<String> lore = weaponMeta.getLore();
                for (String line : lore) {
                    if (line.contains(ChatColor.stripColor("Damage:"))) {
                        String damageStr = line.split(":")[1].trim();
                        double weaponDamage = Double.parseDouble(damageStr);
                        baseDamage += weaponDamage;
                    }
                }
            }
        }
        double damage = baseDamage;
        double strength = stats.getStrength(entity);
        if (weapon != null && weapon.getType() != Material.AIR) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta.hasLore()) {
                List<String> lore = weaponMeta.getLore();
                for (String line : lore) {
                    if (line.contains(ChatColor.stripColor("Strength:"))) {
                        String damageStr = line.split(":")[1].trim();
                        strength += Double.parseDouble(damageStr);
                        damage *= strength;
                    }
                }
            }
        }
        double critChance = stats.getCritChance(entity);
        double critDamage = stats.getCritDamage(entity);
        if (weapon != null && weapon.getType() != Material.AIR) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta.hasLore()) {
                List<String> lore = weaponMeta.getLore();
                for (String line : lore) {
                    if (line.contains(ChatColor.stripColor("Crit Chance:"))) {
                        String critChanceStr = line.split(":")[1].trim();
                        critChance += Double.parseDouble(critChanceStr);
                    } else if (line.contains(ChatColor.stripColor("Crit Damage:"))) {
                        String critDamageStr = line.split(":")[1].trim();
                        critDamage += Double.parseDouble(critDamageStr);
                    }
                }
            }
        }
        if (Math.random() < critChance) {
            damage *= critDamage;
        }
        System.out.println(damage);
        return damage;
    }


    public void dealCustomDamage(double damage, LivingEntity victim) {
        double customHealth = entityStats.get(victim).getHealth(victim);
        PlayerStats stats = entityStats.get(victim);
        double finalDamage = damage;
        customHealth -= finalDamage;
        stats.setCustomHealth(customHealth, victim);
        if (customHealth <= 0) {
            entityStats.remove(victim);
            victim.setHealth(0);
        }
    }

    public void setEntityStats(Entity entity, PlayerStats stats) {
        entityStats.put((LivingEntity) entity, stats);
    }

    public PlayerStats getEntityStats(Entity entity) {
        return entityStats.get(entity);
    }
}
