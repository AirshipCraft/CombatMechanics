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

import java.util.Map;


public class DamageListener implements Listener {
    Map<Player, PlayerStats> playerStats = new HashMap<>();
    private final Map<Player, PlayerStats> entityStats = new HashMap<>();

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) event.getEntity();


        double health = entity.getHealth();
        health += event.getFinalDamage();
        PlayerStats stats = entityStats.get(entity);
        double customHealth = stats.getHealth();
        double maxHealth = stats.getMaxHealth();

        if (customHealth <= 0) {
            entity.setHealth(0);
            customHealth = maxHealth;
        }

        if (event.getFinalDamage() >= entity.getHealth()) {
            event.setCancelled(true);
        }

        if (health > entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
            health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        }

        entity.setHealth(health);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerStats stats = new PlayerStats(100, 10, 5, 2, 5, 1, 100);
        playerStats.put(player, stats);
        BukkitRunnable actionBarTask = new BukkitRunnable() {
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
        actionBarTask.runTaskTimer((Plugin) this, 0L, 20L);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        PlayerStats stats = playerStats.get(player);
        Entity damager = event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        double damage = calculateDamage(playerStats.get(player), weapon, player.getInventory().getArmorContents());
        dealCustomDamage(damage, player, stats);

    }

    public double calculateDamage(PlayerStats stats, ItemStack weapon, ItemStack[] armor) {
        double baseDamage = stats.getBaseDamage();
        if (weapon != null && weapon.getType() != Material.AIR) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta.hasLore()) {
                List<String> lore = weaponMeta.getLore();
                for (String line : lore) {
                    if (line.contains("Damage:")) {
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
                    if (line.contains("Strength:")) {
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
                    if (line.contains("Crit Chance:")) {
                        String critChanceStr = line.split(":")[1].trim();
                        critChance = Double.parseDouble(critChanceStr);
                    } else if (line.contains("Crit Damage:")) {
                        String critDamageStr = line.split(":")[1].trim();
                        critDamage = Double.parseDouble(critDamageStr);
                    }
                }
            }
        }
        // Apply crit damage if a random number is less than the crit chance
        if (Math.random() < critChance) {
            damage *= critDamage;
        }
        return damage;
    }


    public void dealCustomDamage(double damage, Player player, PlayerStats stats) {
        stats.setHealth(stats.getHealth() - damage);
        Location location = player.getLocation().add(0, 1.5, 0);
        World world = player.getWorld();
        ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomName(ChatColor.RED + "-" + damage);
        armorStand.setCustomNameVisible(true);
        Bukkit.getScheduler().runTaskLater((Plugin) this, () -> {
            armorStand.remove();
        }, 20L);
    }
}
