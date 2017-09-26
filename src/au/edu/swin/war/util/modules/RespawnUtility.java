package au.edu.swin.war.util.modules;

import au.edu.swin.war.WarPlayerPlus;
import au.edu.swin.war.event.MatchPlayerRespawnEvent;
import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.util.WarMatch;
import au.edu.swin.war.framework.util.WarModule;
import au.edu.swin.war.util.Manager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
public class RespawnUtility extends WarModule implements Listener {

    private final HashMap<UUID, DeathInfo> info;

    /**
     * Most utilities do not need constructors,
     * but this one has one since the respawn
     * task needs to be initiated when the program
     * starts.
     *
     * @param main Running instance of Manager.
     */
    public RespawnUtility(Manager main) {
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
    public void onDeath(WarPlayer pl) {
        if (pl.getPlayer().getGameMode() != GameMode.SURVIVAL) return; // Ignore non-match deaths.
        pl.getPlayer().setHealth(20); // Revive them, since they don't technically "die".
        main().items().clear(pl); // Clear their inventory.
        pl.getPlayer().setGameMode(GameMode.SPECTATOR); // Temporarily make them a spectator.
        pl.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 400, 1)); // Death effect.

        // Create and store their death information.
        DeathInfo inf = new DeathInfo((WarPlayerPlus) pl);
        info.put(pl.getPlayer().getUniqueId(), inf);
        if (!main().match().getCurrentMode().isPermaDeath())
            pl.getPlayer().sendMessage("You died! Respawning in " + inf.timeUntilRespawn + " second" + main().strings().plural(inf.timeUntilRespawn));
        else
            pl.getPlayer().sendMessage("You have been ejected from the round.");

    }

    /**
     * Runs the respawn task. The basic logic is:
     * For every single player being tracked:
     * How many more seconds until they respawn?
     * If it's 0 seconds, respawn them.
     * If it's more than 0 seconds, subtract one second.
     */
    private void startTask() {
        Bukkit.getScheduler().runTaskTimer(main().plugin(), () -> {
            HashMap<UUID, DeathInfo> inf = new HashMap<>(info);
            // Temporarily duplicate this key/value set to avoid concurrent errors.
            for (Map.Entry<UUID, DeathInfo> entry : inf.entrySet()) {
                info.get(entry.getKey()).timeUntilRespawn--; // Decrement the player's respawn time.
                if (info.get(entry.getKey()).timeUntilRespawn == 0) { // Time to respawn?
                    info.remove(entry.getKey());
                    if (main().match().getStatus() == WarMatch.Status.PLAYING) // Respawn them if the match is still playnig.
                        Bukkit.getPluginManager().callEvent(new MatchPlayerRespawnEvent(main().getWarPlayer(entry.getKey())));
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
    void clearFor(WarPlayer toClear) {
        info.remove(toClear.getPlayer().getUniqueId());
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

        DeathInfo(WarPlayerPlus wpp) {
            timeUntilRespawn = 6 + (int) Math.ceil(wpp.stats().getCurrentStreak() / 2);
        }

        int timeUntilRespawn; // Respawn hell.
    }
}
