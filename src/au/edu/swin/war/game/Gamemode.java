package au.edu.swin.war.game;

import au.edu.swin.war.event.MatchPlayerRespawnEvent;
import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarMode;
import au.edu.swin.war.framework.game.WarTeam;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * An extension to WarMode.
 * <p>
 * This is the class that should be extended by
 * actual gamemode classes to provide a skeleton
 * and good accessibility + functionality.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarMode
 * <p>
 * Created by Josh on 20/04/2017.
 * @since 1.0
 */
public abstract class Gamemode extends WarMode {

    /**
     * This is an enumeration of all available gamemodes.
     * When designating gamemodes for your map, use this.
     */
    public enum Mode {
        TDM("Team Death Match"),
        FFA("Free For All"),
        LTS("Last Team Standing"),
        LMS("Last Man Standing"),
        KOTH("King of The Hill"),
        DDM("District Death Match"),
        CTF("Capture The Flag"),
        DTM("Destroy The Monument");

        String fullName;

        Mode(String fullName) {
            this.fullName = fullName;
        }

        /**
         * Returns the full name of the enumerated type.
         *
         * @return Full name.
         */
        public String getFullName() {
            return fullName;
        }
    }

    @EventHandler
    public void playerDeathHandle(PlayerDeathEvent event) {
        Player dead = event.getEntity(); // Get the player who died.
        Player killer = dead.getKiller(); // Get the player who killed the player.

        if (dead == killer) killer = null; // Did they kill themselves?
        String kn = killer != null ? killer.getName() : "???"; // Assign the killer's name.
        String kdp = killer != null ? killer.getDisplayName() : "???"; // Assign the killer's display name.

        // Play a sound effect.
        dead.getWorld().playSound(dead.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1L, 1L);

        // Format the death message to show display names instead.
        event.setDeathMessage(event.getDeathMessage()
                .replaceAll(dead.getName(), dead.getDisplayName()).replaceAll(kn, kdp));

        // Call the onKill() procedure so the extended Gamemode can react to it.
        onKill(main.getWarPlayer(dead), main.getWarPlayer(killer));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.getPlayer().setBedSpawnLocation(null); // No bed spawn locations.


        WarPlayer wp = main.getWarPlayer(event.getPlayer()); // Get their WarPlayer implement.

        // Change the respawn location to a random team spawn.
        event.setRespawnLocation(randomSpawnFrom(
                main.cache().getCurrentMap().getTeamSpawns(
                        wp.getCurrentTeam().getTeamName())).toLocation(
                main.match().getCurrentWorld(), true));

        // Apply the current map's inventory.
        main.cache().getCurrentMap().applyInv(wp);

        // They're back in the round again!
        event.getPlayer().setGameMode(GameMode.SURVIVAL);
    }

    @EventHandler
    public void onRespawn(MatchPlayerRespawnEvent event) {
        WarPlayer wp = event.getPlayer(); // Get their WarPlayer implement.

        if (!wp.isPlaying()) return; // Ignore this if they aren't playing.

        // Teleport them to their respawn location.
        wp.getPlayer().teleport(randomSpawnFrom(
                map().getTeamSpawns(
                        wp.getCurrentTeam().getTeamName())).toLocation(
                main.match().getCurrentWorld(), true));

        // Apply the current map's inventory.
        map().applyInv(wp);

        // They're back in the round again!
        wp.getPlayer().setGameMode(GameMode.SURVIVAL);
    }

    /**
     * Returns an inputted team's opposition.
     * This method should only be used in a 2-team match.
     *
     * @param team The team to check for opposition.
     * @return The opposition, if any.
     */
    public String opposition(WarTeam team) {
        for (WarTeam teams : map().getTeams())
            if (!team.getTeamName().equals(teams.getTeamName()))
                return teams.getTeamColor() + teams.getTeamName();
        return ChatColor.WHITE + "Unknown";
    }
}
