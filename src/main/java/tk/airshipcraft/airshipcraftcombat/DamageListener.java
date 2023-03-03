package tk.airshipcraft.airshipcraftcombat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Location;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class DamageListener implements Listener {

    public static Map<UUID, PlayerStats> entityStats = new HashMap<>();
    private final BukkitRunnable actionBarTask = new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                PlayerStats playerStats = entityStats.get(uuid);
                double health = playerStats.getHealth(player);
                double maxHealth = playerStats.getMaxHealth(player);
                double defense = playerStats.getDefense(player);
                double percentage = health / maxHealth * 100.0;
                String message = String.format(ChatColor.RED + "Health: %.1f / %.1f (%.0f%%)" + ChatColor.GREEN + " Defense: %.1f", health, maxHealth, percentage, defense);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            }
        }
    };
    public static class HealthRegenTask extends BukkitRunnable {

        private final double healthRegenAmount;

        public HealthRegenTask(double healthRegenAmount) {
            this.healthRegenAmount = healthRegenAmount;
        }

        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerStats playerStats = entityStats.get(player.getUniqueId());
                LivingEntity livingEntity = player;
                double health = playerStats.getHealth(player);
                double maxHealth = playerStats.getMaxHealth(player);
                if (health < maxHealth) {
                    double newHealth = Math.min(health + healthRegenAmount, maxHealth);
                    playerStats.setCustomHealth(newHealth, livingEntity);
                }
            }
        }
    }
    public static double calculateHealthRegen(double maxHealth) {
        double healthRegen = 5 + (0.02 * maxHealth);
        return healthRegen;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = new PlayerStats(100, 10, 5, 2, 5, 1, 100);
        entityStats.put(player.getUniqueId(), stats);
        actionBarTask.runTaskTimer(Main.getPlugin(Main.class), 0L, 20L);
        double healthRegen = calculateHealthRegen(stats.getMaxHealth(player));
        HealthRegenTask regenTask = new HealthRegenTask(healthRegen);
        regenTask.runTaskTimer(Main.getPlugin(Main.class), 0L, 20L);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
    }
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = new PlayerStats(100, 10, 5, 2, 5, 1, 100);
        entityStats.put(player.getUniqueId(), stats);
        actionBarTask.runTaskTimer(Main.getPlugin(Main.class), 0L, 20L);
        player.spigot().sendMessage(ChatMessageType.valueOf(ChatColor.GREEN + "[AirshipCraft] Your stats have successfully loaded!"));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 2.0f);
    }
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            if (event.getEntity() instanceof LivingEntity) {
                if (!(event.getEntity() instanceof ArmorStand)) {
                    LivingEntity entity = (LivingEntity) event.getEntity();
                    PlayerStats stats = new PlayerStats(100.0, 10.0, 0.2, 2.0, 5.0, 1.0, 100.0);
                    entityStats.put(entity.getUniqueId(), stats);
                    UUID uuid = entity.getUniqueId();
                    for (UUID uuidkey : entityStats.keySet()) {
                        PlayerStats stats2 = entityStats.get(uuidkey);
                        double health = stats2.getHealth(entity);
                        double maxHealth = stats2.getMaxHealth(entity);
                        double percentage = health / maxHealth * 100.0;
                        String message = String.format(ChatColor.RED + "Health: %.1f / %.1f (%.0f%%)", health, maxHealth, percentage);
                        entity.setCustomName(message);
                        entity.setCustomNameVisible(true);
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
            UUID uuid = victim.getUniqueId();
            double vanillaDamage = event.getDamage();
            event.setDamage(0.0);
            dealCustomDamage(damage, victim);
            showDamageIndicator(victim, damage);
            if (entityStats.get(uuid) == null) {
                PlayerStats playerStats = new PlayerStats(100.0, 10.0, 0.2, 2.0, 5.0, vanillaDamage, 100.0);
                entityStats.put(uuid, playerStats);
            }
        }
    }
    public static void showDamageIndicator(LivingEntity entity, double damage) {
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
            damage = calculateDamage(entityStats.get(player.getUniqueId()), weapon, null, player);
        } else {

            LivingEntity livingShooter = (LivingEntity) shooter;
            damage = calculateDamage(entityStats.get(livingShooter.getUniqueId()), null, null, livingShooter);
        }
        Entity hitEntity = event.getHitEntity();
        if (hitEntity instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) hitEntity;
            dealCustomDamage(damage, victim);
            showDamageIndicator(victim, damage);
        }
    }
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
    }
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        double vanillaDamage = event.getDamage();
        event.setDamage(0.0);
        Entity damager = event.getDamager();
        if (!(damager instanceof LivingEntity)) {
            return;
        }
        LivingEntity attacker = (LivingEntity) damager;

        LivingEntity victim = (LivingEntity) event.getEntity();
        UUID uuidVictim = victim.getUniqueId();

        PlayerStats victimStats = entityStats.get(uuidVictim);
        PlayerStats attackerStats = null;
        if (entityStats.get(uuidVictim) == null) {
            PlayerStats playerStats = new PlayerStats(100.0, 10.0, 0.2, 2.0, 5.0, vanillaDamage, 100.0);
            entityStats.put(uuidVictim, playerStats);
        }

        if (attacker instanceof Player) {
            attackerStats = entityStats.get(attacker.getUniqueId());
            Player playerAttacker = (Player) attacker;
            attackerStats.setCustomBaseDamage(vanillaDamage, playerAttacker);
            ItemStack weapon = playerAttacker.getInventory().getItemInMainHand();
            ItemStack[] armor = playerAttacker.getInventory().getArmorContents();

            try {
                double damage = calculateDamage(attackerStats, weapon, armor, victim);
                dealCustomDamage(damage + vanillaDamage, victim);
                showDamageIndicator(victim, damage + vanillaDamage);
            } catch (Exception e) {

            }
        } else {
            attackerStats = entityStats.get(attacker.getUniqueId());

            try {
                double damage = calculateDamage(attackerStats, null, null, victim);
                dealCustomDamage(damage + vanillaDamage, victim);
                showDamageIndicator(victim, damage + vanillaDamage);
            } catch (Exception e) {

            }
        }

        double customHealth = victimStats.getHealth(victim);
        double maxHealth = victimStats.getMaxHealth(victim);
        if (customHealth <= 0) {
            victim.setHealth(0);
        }

        for (UUID entityKey : entityStats.keySet()) {
            if (!(victim instanceof ArmorStand)) {
                double percentage = customHealth / maxHealth * 100.0;
                String message = String.format(ChatColor.RED + "Health: %.1f / %.1f (%.0f%%)", customHealth, maxHealth, percentage);
                victim.setCustomName(message);
                victim.setCustomNameVisible(true);
            }
        }
    }


    public static double calculateDamage(PlayerStats stats, ItemStack weapon, ItemStack[] armor, LivingEntity entity) {
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
        double defense = stats.getDefense(entity);
        if (!(defense == 0)) {
            damage = damage / defense;
        }
        return damage;
        }


    public static void dealCustomDamage(double damage, LivingEntity victim) {
        UUID victimKey = victim.getUniqueId();
        if (entityStats.containsKey(victimKey)) {
            double customHealth = entityStats.get(victimKey).getHealth(victim);
            PlayerStats stats = entityStats.get(victimKey);
            double finalDamage = damage;
            customHealth -= finalDamage;
            stats.setCustomHealth(customHealth, victim);
            if (customHealth <= 0) {
                entityStats.remove(victimKey);
                victim.setHealth(0);
            }
        } else {
            System.out.println("Error: " + victim + "'s stats don't exist");
        }
    }
    public static PlayerStats getEntityStats(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        return entityStats.get(uuid);
    }
    public static void setEntityStats(LivingEntity entity, PlayerStats stats) {
        UUID uuid = entity.getUniqueId();
        entityStats.put(uuid, stats);
    }
}
