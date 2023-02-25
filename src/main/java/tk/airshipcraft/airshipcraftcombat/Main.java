package tk.airshipcraft.airshipcraftcombat;

import org.bukkit.plugin.java.JavaPlugin;


public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new DamageListener(), this);
    }



    @Override
    public void onDisable() {

    }
}
