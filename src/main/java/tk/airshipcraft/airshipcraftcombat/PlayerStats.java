package tk.airshipcraft.airshipcraftcombat;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.UUID;

public class PlayerStats {


    private HashMap<UUID, Double> healthMap;
    private HashMap<UUID, Double> strengthMap;
    private HashMap<UUID, Double> critChanceMap;
    private HashMap<UUID, Double> critDamageMap;
    private HashMap<UUID, Double> defenseMap;
    private HashMap<UUID, Double> baseDamageMap;
    private HashMap<UUID, Double> maxHealthMap;

    public PlayerStats(double health, double strength, double critChance, double critDamage, double defense, double baseDamage, double maxHealth) {
        this.healthMap = new HashMap<>();
        this.strengthMap = new HashMap<>();
        this.critChanceMap = new HashMap<>();
        this.critDamageMap = new HashMap<>();
        this.defenseMap = new HashMap<>();
        this.baseDamageMap = new HashMap<>();
        this.maxHealthMap = new HashMap<>();
    }

    public double getHealth(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        if (healthMap.containsKey(uuid)) {
            return healthMap.get(uuid);
        } else {
            if (!(entity instanceof ArmorStand)) {
                healthMap.put(uuid, 100.0);
                return healthMap.get(uuid);
            }
        }
        return 0.0;
    }


    public double getMaxHealth(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        if (maxHealthMap.containsKey(uuid)) {
            return maxHealthMap.get(uuid);

        } else {
            if (!(entity instanceof ArmorStand)) {
                maxHealthMap.put(uuid, 100.0);
                return maxHealthMap.get(uuid);
            }
        }
        return 0.0;
    }

    public double getBaseDamage(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        if (baseDamageMap.containsKey(uuid)) {
            return baseDamageMap.get(uuid);

        }else {
            if (!(entity instanceof ArmorStand)) {
                baseDamageMap.put(uuid, 1.0);
                return baseDamageMap.get(uuid);
            }
        }
        return 0.0;
    }

    public void setCustomHealth(double health, LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        healthMap.put(uuid, health);
    }

    public void setCustomStrength(double strength, LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        strengthMap.put(uuid, strength);
    }

    public void setCustomCritChance(double critChance, LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        critChanceMap.put(uuid, critChance);
    }

    public void setCustomCritDamage(double critDamage, LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        critDamageMap.put(uuid, critDamage);
    }

    public void setCustomDefense(double defense, LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        defenseMap.put(uuid, defense);
    }

    public void setCustomBaseDamage(double baseDamage, LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        baseDamageMap.put(uuid, baseDamage);
    }

    public void setCustomMaxHealth(double maxHealth, LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        maxHealthMap.put(uuid, maxHealth);
    }

    public double getStrength(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        if (strengthMap.containsKey(uuid)) {
            return strengthMap.get(uuid);

        }else{
            if (!(entity instanceof ArmorStand)) {
                strengthMap.put(uuid, 10.0);
                return strengthMap.get(uuid);
            }
        }return 0.0;
    }

    public double getCritChance(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        if (critChanceMap.containsKey(uuid)) {
            return critChanceMap.get(uuid);
        } else {
            if (!(entity instanceof ArmorStand)) {
                critChanceMap.put(uuid, 0.2);
                return critChanceMap.get(uuid);
            }
        }return 0.0;
    }

    public double getCritDamage(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        if (critDamageMap.containsKey(uuid)) {
            return critDamageMap.get(uuid);

        }else{
            if (!(entity instanceof ArmorStand)) {
                critDamageMap.put(uuid, 2.0);
                return critDamageMap.get(uuid);
            }
        }return 0.0;
    }

    public double getDefense(LivingEntity entity) {
        UUID uuid = entity.getUniqueId();
        if (defenseMap.containsKey(uuid)) {
            return defenseMap.get(uuid);
        }else{
            if (!(entity instanceof ArmorStand)) {
                defenseMap.put(uuid, 0.0);
                return defenseMap.get(uuid);
            }
        }return 0.0;
    }
}
