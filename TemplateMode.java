package au.edu.swin.war.game.modes;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * An extension to gamemode to implement an
 * actual gamemode.
 *
 * @author x
 * @version 1.0
 * @see WarManager
 * <p>
 * @since 1.0
 */
public class TemplateMode extends Gamemode {

    private final HashMap<String, Integer> points = new HashMap<>();
    // Key/value set to hold teams' points.
    // Not intrinsically needed, but it is a good starting point.

    /**
     * Reset your values here.
     */
    public void reset() {
        // Clears the "points" key/value set for next time this gamemode is played.
        points.clear();
    }

    /**
     * Initialisation occurs here.
     */
    public void initialize() {
        for (WarTeam team : getTeams()) // Give every participating team a default score of zero.
            points.put(team.getTeamName(), 0);

        //TODO: Make this method automatic?
        // Keep a temporary list of people who have not being assigned to a team.
        ArrayList<WarPlayer> targets = new ArrayList<>(main.getWarPlayers().values());
        while (targets.size() != 0) { // Keep looping until this array is empty.
            WarPlayer target = targets.get(rng.nextInt(targets.size())); // Gets a random player.
            if (target.isJoined()) {
                // If joined, use entryHandle() to put them on the lowest team.
                entryHandle(target);
                if (!target.isJoined()) {
                    // If, for some reason, they did not get put on a team, assume them as spectating.
                    target.getPlayer().setGameMode(GameMode.SPECTATOR);
                    main.giveSpectatorKit(target);
                }
            } else {
                // They don't want to play. Assume them as spectating.
                target.getPlayer().setGameMode(GameMode.SPECTATOR);
                main.giveSpectatorKit(target);
            }
            targets.remove(target);
        }

        // Assign objective to scoreboard for this gamemode.
        Objective obj = s().registerNewObjective("gm", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR); // Display it in sidebar. Pretty.
        updateScoreboard(); // Update the scoreboard to put all the default values on it.

        for (Player online : Bukkit.getOnlinePlayers())
            online.setScoreboard(s()); // Everyone online needs to see this scoreboard.
    }

    /**
     * Called by the automatic task every second.
     */
    public void tick() {
        // Include code you want ran every second here.
    }

    /**
     * Called when a player kills another player.
     *
     * @param killed The player who died.
     * @param killer The player's killer.
     */
    public void onKill(WarPlayer killed, WarPlayer killer) {
        // Include code you want ran every time a player is killed here.
    }


    /**
     * Called when a player dies with no player cause.
     *
     * @param killed The player who died.
     */
    public void onDeath(WarPlayer killed) {
        // Include code you want ran every time a player dies here.
    }

    /**
     * Decide who won the match here.
     */
    public void decideWinner() {
        // Decide who was the winner here.
        // This involves setting tempWinner and broadcasting a win.
    }

    public String getOffensive() {
        return "<insert offensive>";
    }

    public String getDefensive() {
        return "<insert defensive>";
    }

    public String getName() {
        return "MGM";
    }

    public String getFullName() {
        return "My Gamemode";
    }

    public String getGrammar() {
        return "an";
    }

    /**
     * Called when a player leaves the match.
     *
     * @param left The player who left.
     */
    public void onLeave(WarPlayer left) {
        // Include code you want ran every time a player leaves here.
    }

    /**
     * Called as needed by your gamemode to update
     * the scoreboard with appropriate values.
     */
    public void updateScoreboard() {
        // Gamemodes are encouraged to include scoreboards.
        // See the other default gamemodes for examples.
    }

    /**
     * Any extra team-related statistics to add to the script?
     *
     * @param team The team associated with the data.
     * @return The extra data, if any.
     */
    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        // Put any extra information related to the team here.
        // See other default gamemodes for examples.
        return extra;
    }
}
