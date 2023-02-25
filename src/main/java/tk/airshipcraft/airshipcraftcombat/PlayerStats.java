package tk.airshipcraft.airshipcraftcombat;


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
            System.out.println("Health not found for player " + playerUUID.getName());
            System.out.println("Putting new health for player" + playerUUID.getName());
            healthMap.put(playerUUID, 100.0);
            return 100.0;
        }
    }



    public double getMaxHealth(LivingEntity entity) {
        return maxHealthMap.getOrDefault(entity, defaultMaxHealth);
    }

    public double getBaseDamage(LivingEntity entity) {
        return baseDamageMap.getOrDefault(entity, defaultBaseDamage);
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
        return strengthMap.getOrDefault(entity, defaultStrength);
    }

    public double getCritChance(LivingEntity entity) {
        return critChanceMap.getOrDefault(entity, defaultCritChance);
    }

    public double getCritDamage(LivingEntity entity) {
        return critDamageMap.getOrDefault(entity, defaultCritDamage);
    }

    public double getDefense(LivingEntity entity) {
        return defenseMap.getOrDefault(entity, defaultDefense);
    }
}
