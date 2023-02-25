package tk.airshipcraft.airshipcraftcombat;


import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;

public class PlayerStats {
    private double defaultHealth;
    private double defaultStrength;
    private double defaultCritChance;
    private double defaultCritDamage;
    private double defaultDefense;
    private double defaultBaseDamage;
    private double defaultMaxHealth;

    private HashMap<LivingEntity, Double> healthMap;
    private HashMap<LivingEntity, Double> strengthMap;
    private HashMap<LivingEntity, Double> critChanceMap;
    private HashMap<LivingEntity, Double> critDamageMap;
    private HashMap<LivingEntity, Double> defenseMap;
    private HashMap<LivingEntity, Double> baseDamageMap;
    private HashMap<LivingEntity, Double> maxHealthMap;

    public PlayerStats(double health, double strength, double critChance, double critDamage, double defense, double baseDamage, double maxHealth) {
        this.healthMap = new HashMap<>();
        this.strengthMap = new HashMap<>();
        this.critChanceMap = new HashMap<>();
        this.critDamageMap = new HashMap<>();
        this.defenseMap = new HashMap<>();
        this.baseDamageMap = new HashMap<>();
        this.maxHealthMap = new HashMap<>();

        this.defaultHealth = health;
        this.defaultStrength = strength;
        this.defaultCritChance = critChance;
        this.defaultCritDamage = critDamage;
        this.defaultDefense = defense;
        this.defaultBaseDamage = baseDamage;
        this.defaultMaxHealth = maxHealth;
    }

    public double getHealth(LivingEntity playerUUID) {
        if (healthMap.containsKey(playerUUID)) {
            return healthMap.get(playerUUID);
        } else {
            if (!(playerUUID instanceof ArmorStand)) {
                healthMap.put(playerUUID, 100.0);
                return healthMap.get(playerUUID);
            }
        }
        return 0.0;
    }


    public double getMaxHealth(LivingEntity entity) {

        if (maxHealthMap.containsKey(entity)) {
            return maxHealthMap.get(entity);

        } else {
            if (!(entity instanceof ArmorStand)) {
                maxHealthMap.put(entity, 100.0);
                return maxHealthMap.get(entity);
            }
        }
        return 0.0;
    }

    public double getBaseDamage(LivingEntity entity) {
        if (baseDamageMap.containsKey(entity)) {
            return baseDamageMap.get(entity);

        }else {
            if (!(entity instanceof ArmorStand)) {
                baseDamageMap.put(entity, 1.0);
                return baseDamageMap.get(entity);
            }
        }
        return 0.0;
    }

    public void setCustomHealth(double health, LivingEntity entity) {
        healthMap.put(entity, health);
    }

    public void setCustomStrength(double strength, LivingEntity entity) {
        strengthMap.put(entity, strength);
    }

    public void setCustomCritChance(double critChance, LivingEntity entity) {
        critChanceMap.put(entity, critChance);
    }

    public void setCustomCritDamage(double critDamage, LivingEntity entity) {
        critDamageMap.put(entity, critDamage);
    }

    public void setCustomDefense(double defense, LivingEntity entity) {
        defenseMap.put(entity, defense);
    }

    public void setCustomBaseDamage(double baseDamage, LivingEntity entity) {
        baseDamageMap.put(entity, baseDamage);
    }

    public void setCustomMaxHealth(double maxHealth, LivingEntity entity) {
        maxHealthMap.put(entity, maxHealth);
    }

    public double getStrength(LivingEntity entity) {
        if (strengthMap.containsKey(entity)) {
            return strengthMap.get(entity);

        }else{
            if (!(entity instanceof ArmorStand)) {
                strengthMap.put(entity, 10.0);
                return strengthMap.get(entity);
            }
        }return 0.0;
    }

    public double getCritChance(LivingEntity entity) {
        if (critChanceMap.containsKey(entity)) {
            return critChanceMap.get(entity);
        } else {
            if (!(entity instanceof ArmorStand)) {
                critChanceMap.put(entity, 0.2);
                return critChanceMap.get(entity);
            }
        }return 0.0;
    }

    public double getCritDamage(LivingEntity entity) {
        if (critDamageMap.containsKey(entity)) {
            return critDamageMap.get(entity);
            
        }else{
            if (!(entity instanceof ArmorStand)) {
                critDamageMap.put(entity, 2.0);
                return critDamageMap.get(entity);
            }
        }return 0.0;
    }

    public double getDefense(LivingEntity entity) {
        if (defenseMap.containsKey(entity)) {
            return defenseMap.get(entity);
        }else{
            if (!(entity instanceof ArmorStand)) {
                defenseMap.put(entity, 0.0);
                return defenseMap.get(entity);
            }
        }return 0.0;
    }
}
