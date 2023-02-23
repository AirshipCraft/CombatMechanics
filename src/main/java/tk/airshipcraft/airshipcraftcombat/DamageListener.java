package tk.airshipcraft.airshipcraftcombat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Map;

@SuppressWarnings("unused")
public class DamageListener implements Listener {
    private final Plugin plugin;
    Map<Player, PlayerStats> playerStats = new HashMap<>();
    private final Map<LivingEntity, PlayerStats> entityStats = new HashMap<>();

    private final BukkitRunnable actionBarTask = new BukkitRunnable() {
        @Override
        public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerStats stats = playerStats.get(player);
                double health = stats.getHealth();
                double maxHealth = stats.getMaxHealth();
                double percentage = health / maxHealth * 100.0;
                String message = String.format("Health: %.1f / %.1f (%.0f%%)", health, maxHealth, percentage);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            }
        }
    };


    public DamageListener(Plugin plugin){
        this.plugin = plugin;
    }
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) event.getEntity();
        double health = entity.getHealth();
        health += event.getFinalDamage();

        PlayerStats stats = entityStats.get(entity);
        PlayerStats damagerStats = null;
        Entity damager = null;
        ItemStack weapon = null;
        ItemStack[] armor = null;
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager instanceof Player) {
                damagerStats = playerStats.get(damager);
                Player playerDamager = (Player) damager;
                weapon = playerDamager.getInventory().getItemInMainHand();
                armor = playerDamager.getInventory().getArmorContents();

            } else {
                damagerStats = entityStats.get(damager);
            }
        }

        double customHealth = stats.getHealth();
        double maxHealth = stats.getMaxHealth();
        if (damagerStats == null) {

        } else {
            try {
                double damage = calculateDamage(damagerStats, weapon, armor);
                dealCustomDamage(damage, entity, damagerStats);
            }
            catch(Exception e) {
                throw new RuntimeException(damager + "'s stats don't exist, please tell him to relog");
            }
        }

        if (customHealth <= 0) {
            entity.setHealth(0);
            stats.setHealth(maxHealth);
        }

        if (health > entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
            health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        }
        for (LivingEntity entityKey : entityStats.keySet()) {
            double percentage = health / maxHealth * 100.0;
            String message = String.format(ChatColor.RED + "Health: %.1f / %.1f (%.0f%%)", health, maxHealth, percentage);
            entityKey.setCustomName(message);
            entityKey.setCustomNameVisible(true);
        }


        entity.setHealth(health);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = new PlayerStats(100, 10, 5, 2, 5, 1, 100);
        playerStats.put(player, stats);
        System.out.println(playerStats);
        System.out.println(player + "'s stats have successfully loaded");
        actionBarTask.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            if (event.getEntity() instanceof ArmorStand) {

            } else {
                LivingEntity entity = (LivingEntity) event.getEntity();
                PlayerStats stats = new PlayerStats(100, 10, 5, 2, 5, 1, 100);
                entityStats.put(entity, stats);
                for (LivingEntity entity2 : entityStats.keySet()) {
                    PlayerStats stats2 = entityStats.get(entity2);
                    double health = stats2.getHealth();
                    double maxHealth = stats2.getMaxHealth();
                    double percentage = health / maxHealth * 100.0;
                    String message = String.format(ChatColor.RED + "Health: %.1f / %.1f (%.0f%%)", health, maxHealth, percentage);
                    entity2.setCustomName(message);
                    entity2.setCustomNameVisible(true);
                }
            }

        }
    }

    public double calculateDamage(PlayerStats stats, ItemStack weapon, ItemStack[] armor) {
        double baseDamage = stats.getBaseDamage();
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
        double strength = stats.getStrength();
        if (weapon != null && weapon.getType() != Material.AIR) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta.hasLore()) {
                List<String> lore = weaponMeta.getLore();
                for (String line : lore) {
                    if (line.contains(ChatColor.stripColor("Strength:"))) {
                        String damageStr = line.split(":")[1].trim();
                        strength += Double.parseDouble(damageStr);
                        damage *= strength / 100;
                    }
                }
            }
        }
        double critChance = stats.getCritChance();
        double critDamage = stats.getCritDamage();
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
            damage *= critDamage / 100;
        }
        return damage;
    }


    public void dealCustomDamage(double damage, LivingEntity player, PlayerStats stats) {
        stats.setHealth(stats.getHealth() - damage);
        Location location = player.getLocation().add(0, 0.5, 0);
        World world = player.getWorld();
        ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomName(ChatColor.RED + "-" + damage + " Damage");
        armorStand.setCustomNameVisible(true);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            armorStand.remove();
        }, 20L);
    }

}
