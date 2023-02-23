package tk.airshipcraft.airshipcraftcombat;

import org.bukkit.entity.Player;

public class PlayerStats {
    private double maxHealth;
    private final double baseDamage;
    private double health;
    private double strength;
    private double critChance;
    private double critDamage;
    private double defense;

    public PlayerStats(double health, double strength, double critChance, double critDamage, double defense, double baseDamage, double maxHealth) {
        this.health = health;
        this.strength = strength;
        this.critChance = critChance;
        this.critDamage = critDamage;
        this.defense = defense;
        this.baseDamage = baseDamage;
        this.maxHealth = maxHealth;
    }

    public double getHealth() {
        return health;
    }
    public double getMaxHealth() {
        return maxHealth;
    }
    public double getBaseDamage() {
        return baseDamage;
    }
    public void setHealth(double health) {
        this.health = health;
    }

    public double getStrength() {
        return strength;
    }

    public void setStrength(double strength) {
        this.strength = strength;
    }
    public double getCritChance() {
        return critChance;
    }

    public void setCritChance(double critChance) {
        this.critChance = critChance;
    }

    public double getCritDamage() {
        return critDamage;
    }

    public void setCritDamage(double critDamage) {
        this.critDamage = critDamage;
    }

    public double getDefense() {
        return defense;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }
}

