package tk.airshipcraft.airshipcraftcombat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
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

public class DamageListener implements Listener {
    private static final double MAX_HEALTH = 100.0;
    private static final double HEALTH = 100.0;
    private static final String HEALTH_BAR_FORMAT = ChatColor.GREEN + "Health: %.1f";
    private static final String HEALTH_BAR_TITLE = "Health";
    private static final int ACTIONBAR_UPDATE_INTERVAL = 3;

    // Constants for damage calculation
    private static final double BASE_DAMAGE = 1;
    private static final double CRIT_CHANCE = 0.1;
    private static final double CRIT_DAMAGE_MULTIPLIER = 20.0;
    private static final double STRENGTH_DAMAGE_MULTIPLIER = 1;

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        double customHealth = HEALTH;
        LivingEntity entity = (LivingEntity) event.getEntity();


        double health = entity.getHealth();
        health += event.getFinalDamage();

        if (customHealth <= 0) {
            entity.setHealth(0);
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
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        double customHealth = HEALTH;
        Player player = (Player) event.getEntity();
        Entity damager = event.getDamager();
        ItemStack weapon = player.getInventory().getItemInMainHand();
        double damage = calculateDamage(player, weapon, player.getInventory().getArmorContents());
        event.setDamage(damage);
    }

    public double calculateDamage(Player player, ItemStack weapon, ItemStack[] armor) {
        double baseDamage = BASE_DAMAGE;
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
        if (weapon != null && weapon.getType() != Material.AIR) {
            ItemMeta weaponMeta = weapon.getItemMeta();
            if (weaponMeta.hasLore()) {
                List<String> lore = weaponMeta.getLore();
                for (String line : lore) {
                    if (line.contains("Strength:")) {
                        String damageStr = line.split(":")[1].trim();
                        double strength = Double.parseDouble(damageStr);
                        damage *= strength / 100;
                    }
                }
            }
        }
        double critChance = CRIT_CHANCE;
        double critDamage = CRIT_DAMAGE_MULTIPLIER;
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
        double critRoll = Math.random();
        boolean isCrit = critRoll < critChance;
        if (isCrit) {
            damage *= critDamage / 100;
        }

        return damage;
    }

    public void dealCustomDamage(double damage, Player player, double customHealth) {
        customHealth -= damage;
        if (customHealth < 0) {
            customHealth = 0;
        }
        Location location = player.getLocation().add(0, 1.5, 0);
        World world = player.getWorld();
        ArmorStand armorStand = (ArmorStand) world.spawnEntity(location, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setCustomName(ChatColor.RED + "-" + damage);
        armorStand.setCustomNameVisible(true);
        Bukkit.getScheduler().runTaskLater((Plugin) this, () -> {
            armorStand.remove();
        }, 20L );
    }
}