package tk.airshipcraft.airshipcraftcombat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class DamageListener implements Listener {

    public Map<LivingEntity, PlayerStats> entityStats = new HashMap<>();
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
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = new PlayerStats(100, 10, 5, 2, 5, 1, 100);
        entityStats.put(player, stats);
        actionBarTask.runTaskTimer(Main.getPlugin(Main.class), 0L, 20L);
        player.sendMessage(ChatColor.GREEN + "Your stats have successfully loaded!");
    }


    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
            if (event.getEntity() instanceof ArmorStand) {}
                if (event.getEntity() instanceof Player) {
                } else {
                    if (event.getEntity() instanceof LivingEntity) {
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
