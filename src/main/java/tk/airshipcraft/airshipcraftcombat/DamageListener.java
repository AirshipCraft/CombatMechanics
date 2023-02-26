package tk.airshipcraft.airshipcraftcombat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.*;
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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;

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
        if (!(event.getEntity() instanceof Player)) {
            if (event.getEntity() instanceof LivingEntity) {
                if (!(event.getEntity() instanceof ArmorStand)) {
                    LivingEntity entity = (LivingEntity) event.getEntity();
                    PlayerStats stats = new PlayerStats(100.0, 10.0, 0.2, 2.0, 5.0, 1.0, 100.0);
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
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
        } else {
            double damage = event.getDamage();
            LivingEntity victim = (LivingEntity) event.getEntity();
            event.setDamage(0.0);
            dealCustomDamage(damage, victim);
            showDamageIndicator(victim, damage);
            if (entityStats.get(victim) == null) {
                PlayerStats playerStats = new PlayerStats(100.0, 10.0, 0.2, 2.0, 5.0, 1.0, 100.0);
                entityStats.put(victim, playerStats);
            }

        }
    }
    public void showDamageIndicator(LivingEntity entity, double damage) {
        Location loc = entity.getLocation();
        loc.add(0, entity.getHeight(), 0);
        ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class);
        as.setVisible(false);
        as.setGravity(false);
        as.setCanPickupItems(false);
        as.setMarker(true);
        as.setCustomNameVisible(true);
        as.setInvulnerable(true);
        as.setCustomName("-" + String.format("%.1f", damage));
        as.setRemoveWhenFarAway(true);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(Main.class), new Runnable() {
            @Override
            public void run() {
                as.remove();
            }
        }, 20L);
    }

        @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        LivingEntity shooter = (LivingEntity) projectile.getShooter();
        double damage;
        if (shooter instanceof Player) {
            Player player = (Player) shooter;
            ItemStack weapon = player.getInventory().getItemInMainHand();
            damage = calculateDamage(entityStats.get(player), weapon, null, player);
        } else {

            LivingEntity livingShooter = (LivingEntity) shooter;
            damage = calculateDamage(entityStats.get(livingShooter), null, null, livingShooter);
        }
        Entity hitEntity = event.getHitEntity();
        if (hitEntity instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) hitEntity;
            dealCustomDamage(damage, victim);
            showDamageIndicator(victim, damage);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        event.setDamage(0.0);
        Entity damager = event.getDamager();
        if (!(damager instanceof LivingEntity)) {
            return;
        }
        LivingEntity attacker = (LivingEntity) damager;

        LivingEntity victim = (LivingEntity) event.getEntity();

        PlayerStats victimStats = entityStats.get(victim);
        PlayerStats attackerStats = null;
        if (entityStats.get(victim) == null) {
            PlayerStats playerStats = new PlayerStats(100.0, 10.0, 0.2, 2.0, 5.0, 1.0, 100.0);
            entityStats.put(victim, playerStats);
        }

        if (attacker instanceof Player) {
            attackerStats = entityStats.get(attacker);
            Player playerAttacker = (Player) attacker;
            ItemStack weapon = playerAttacker.getInventory().getItemInMainHand();
            ItemStack[] armor = playerAttacker.getInventory().getArmorContents();

            try {
                double damage = calculateDamage(attackerStats, weapon, armor, victim);
                dealCustomDamage(damage, victim);
                showDamageIndicator(victim, damage);
            } catch (Exception e) {

            }
        } else {
            attackerStats = entityStats.get(attacker);

            try {
                double damage = calculateDamage(attackerStats, null, null, victim);
                dealCustomDamage(damage, victim);
                showDamageIndicator(victim, damage);
            } catch (Exception e) {

            }
        }

        double customHealth = victimStats.getHealth(victim);
        double maxHealth = victimStats.getMaxHealth(victim);
        if (customHealth <= 0) {
            victim.setHealth(0);
        }

        for (LivingEntity entityKey : entityStats.keySet()) {
            if (!(victim instanceof ArmorStand)) {
                double percentage = customHealth / maxHealth * 100.0;
                String message = String.format(ChatColor.RED + "Health: %.1f / %.1f (%.0f%%)", customHealth, maxHealth, percentage);
                entityKey.setCustomName(message);
                entityKey.setCustomNameVisible(true);
            }
        }
    }


    public double calculateDamage(PlayerStats stats, ItemStack weapon, ItemStack[] armor, LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        double baseDamage = stats.getBaseDamage(entity);
        if (weapon != null) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta.hasLore()) {
                List<String> lore = weaponMeta.getLore();
                for (String line : lore) {
                    if (line.contains(ChatColor.stripColor("Damage:"))) {
                        String damageStr = ChatColor.stripColor(line).split(":")[1].trim();
                        double weaponDamage = Double.parseDouble(damageStr);
                        baseDamage += weaponDamage;
                    }
                }
            }
        }
        double damage = baseDamage;
        double strength = stats.getStrength(entity);
        if (weapon != null) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta.hasLore()) {
                List<String> lore = weaponMeta.getLore();
                for (String line : lore) {
                    if (line.contains(ChatColor.stripColor("Strength:"))) {
                        String damageStr = ChatColor.stripColor(line).split(":")[1].trim();
                        strength += Double.parseDouble(damageStr);
                        damage *= strength;
                    }
                }
            }
        }
        double critChance = stats.getCritChance(entity);
        double critDamage = stats.getCritDamage(entity);
        if (weapon != null) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta.hasLore()) {
                List<String> lore = weaponMeta.getLore();
                for (String line : lore) {
                    if (line.contains(ChatColor.stripColor("Crit Chance:"))) {
                        String critChanceStr = ChatColor.stripColor(line).split(":")[1].trim();
                        critChance += Double.parseDouble(critChanceStr);
                    } else if (line.contains(ChatColor.stripColor("Crit Damage:"))) {
                        String critDamageStr = ChatColor.stripColor(line).split(":")[1].trim();
                        critDamage += Double.parseDouble(critDamageStr);
                    }
                }
            }
        }
        if (Math.random() < critChance) {
            damage *= critDamage;
        }
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
