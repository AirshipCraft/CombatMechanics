package tk.airshipcraft.airshipcraftcombat;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public interface DamageListenerInterface {
    Map<LivingEntity, PlayerStats> entityStats = new HashMap<>();

    default void setEntityStats(LivingEntity entity, PlayerStats stats) {
        entityStats.put(entity, stats);
    }
    default PlayerStats getEntityStats(LivingEntity entity) {
        return entityStats.get(entity);
    }
}
