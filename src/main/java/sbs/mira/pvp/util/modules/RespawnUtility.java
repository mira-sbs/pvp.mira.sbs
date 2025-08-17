package sbs.mira.pvp.util.modules;

import sbs.mira.pvp.MiraPvpPlayer;
import sbs.mira.pvp.event.MatchPlayerRespawnEvent;
import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.util.WarMatch;
import sbs.mira.pvp.framework.MiraModule;
import sbs.mira.pvp.MiraPvpMaster;
import net.minecraft.server.v1_12_R1.DataWatcherObject;
import net.minecraft.server.v1_12_R1.DataWatcherRegistry;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class handles cruicial inventory and
 * item-related prodecures as documented below.
 * <p>
 * Created by Josh on 20/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @since 1.0
 */
public class RespawnUtility extends MiraModule implements Listener {

    private final HashMap<UUID, DeathInfo> info;

    /**
     * Most utilities do not need constructors,
     * but this one has one since the respawn
     * task needs to be initiated when the program
     * starts.
     *
     * @param main Running instance of Manager.
     */

    public RespawnUtility(MiraPvpMaster main) {
        super(main);
        main.plugin().getServer().getPluginManager().registerEvents(this, main.plugin());

        // Initialise the key/value set for the respawning task.
        info = new HashMap<>();
        // Start the respawning task!
        startTask();
    }

    /**
     * Handles respawning when a player dies.
     *
     * @param pl The player who died.
     */
    public void onDeath(MiraPlayer pl) {
        if (pl.crafter().getGameMode() != GameMode.SURVIVAL) return; // Ignore non-match deaths.
        pl.crafter().setHealth(20); // Revive them, since they don't technically "die".
        mira().items().clear(pl); // Clear their inventory.
        pl.crafter().setGameMode(GameMode.SPECTATOR); // Temporarily make them a spectator.
        pl.crafter().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 400, 1)); // Death effect.

        // Create and store their death information.
        DeathInfo inf = new DeathInfo((MiraPvpPlayer) pl);
        info.put(pl.crafter().getUniqueId(), inf);
        if (!mira().match().getCurrentMode().isPermaDeath())
            pl.crafter().sendMessage("You died! Respawning in " + inf.timeUntilRespawn + " second" + mira().strings().plural(inf.timeUntilRespawn));
        else
            pl.crafter().sendMessage("You have been ejected from the round.");

    }

    /**
     * Runs the respawn task. The basic logic is:
     * For every single player being tracked:
     * How many more seconds until they respawn?
     * If it's 0 seconds, respawn them.
     * If it's more than 0 seconds, subtract one second.
     */
    private void startTask() {
        Bukkit.getScheduler().runTaskTimer(mira().plugin(), () -> {
            HashMap<UUID, DeathInfo> inf = new HashMap<>(info);
            // Temporarily duplicate this key/value set to avoid concurrent errors.
            for (Map.Entry<UUID, DeathInfo> entry : inf.entrySet()) {
                info.get(entry.getKey()).timeUntilRespawn--; // Decrement the player's respawn time.
                if (info.get(entry.getKey()).timeUntilRespawn == 0) { // Time to respawn?
                    info.remove(entry.getKey());
                    if (mira().match().getStatus() == WarMatch.Status.PLAYING) {
                        // Respawn them if the match is still playing.
                        MiraPlayer respawn = mira().getWarPlayer(entry.getKey());
                        Bukkit.getPluginManager().callEvent(new MatchPlayerRespawnEvent(respawn));
                        ((CraftPlayer) respawn.crafter()).getHandle().getDataWatcher().set(new DataWatcherObject<>(10, DataWatcherRegistry.b),0);
                    }
                    break;
                }
            }
        }, 0L, 20L);
    }

    /**
     * Clear all respawning information.
     * This should be called when a round ends.
     */
    public void clear() {
        info.clear();
    }

    /**
     * Clear respawning information for a player.
     * This should be called when a player leaves.
     *
     * @param toClear The player to get their info removed.
     */
    void clearFor(MiraPlayer toClear) {
        info.remove(toClear.crafter().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Remove their respawn information if they log out as it is no longer needed.
        if (info.containsKey(event.getPlayer().getUniqueId()))
            info.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Simple record that stores an integer.
     * Some day, this class may be extended to hold more info.
     */
    private class DeathInfo {

        DeathInfo(MiraPvpPlayer wpp) {
            timeUntilRespawn = 6 + (int) Math.ceil(wpp.stats().getCurrentStreak() / 2);
        }

        int timeUntilRespawn; // Respawn hell.
    }
}
