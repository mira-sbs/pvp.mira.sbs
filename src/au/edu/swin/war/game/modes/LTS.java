package au.edu.swin.war.game.modes;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * An extension to gamemode to implement LTS.
 * Last Team Standing objectives have been defined
 * in my design brief, so I will assume you
 * know what you are expecting to look at here.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarManager
 * <p>
 * Created by Josh on 26/04/2017.
 * @since 1.0
 */
public class LTS extends Gamemode {

    private ArrayList<UUID> participated; // Keep a list of people who participated.
    private HashMap<String, Integer> original; // Keep a record of how many players originally played.

    public void reset() {
        if (participated != null)
            while (participated.size() > 0) {
                WarPlayer wp = main.getWarPlayer(participated.get(0));
                if (wp != null)
                    wp.setJoined(true); // Re-set this player as joined, since they participated.
                participated.remove(participated.get(0)); // We can remove this player from the list now.
            }
        participated = null; // Remove instance of this list.
        original.clear(); // Clear and remove instance of original participants.
        original = null;
    }

    public void initialize() {
        // Initialize the array and key/value set first!
        participated = new ArrayList<>();
        original = new HashMap<>();

        if (getJoined() < 2) {
            // LTS requires 2 players at the least to play.
            Bukkit.broadcastMessage("There needs to be 2 or more participating players!");
            logEvent("Match cancelled as there was not enough players");
            onEnd();
            return;
        }

        autoAssign();

        for (WarTeam team : getTeams()) // Record the original amount of participants.
            original.put(team.getTeamName(), team.getBukkitTeam().getEntries().size());

        permaDeath = true; // Set permanent death to true for the duration of the match.

        // Assign objective to scoreboard for this gamemode.
        Objective obj = s().registerNewObjective("gm", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR); // Display it in sidebar. Pretty.
        updateScoreboard(); // Update the scoreboard to put all the default values on it.

        for (Player online : Bukkit.getOnlinePlayers())
            online.setScoreboard(s()); // Everyone online needs to see this scoreboard.
    }

    public void tick() {
        //Nothing needed here.
    }

    public void onKill(WarPlayer killed, WarPlayer killer) {
        dead(killed);
    }

    public void onDeath(WarPlayer dead) {
        dead(dead);
    }

    /**
     * Common code is shared by onKill and onDeath,
     * both call to this procedure to prevent duplication.
     *
     * @param dead The player who died.
     */
    private void dead(WarPlayer dead) {
        // Kick them out of the match as this is permanent death.
        dead.setJoined(false);
        entryHandle(dead);
    }

    public void decideWinner() {
        for (WarTeam team : getTeams()) {
            if (team.getBukkitTeam().getEntries().size() >= 1) {
                Bukkit.broadcastMessage(team.getDisplayName() + " is the last team standing!");
                tempWinner = team.getDisplayName();
                return;
            }
        }
        Bukkit.broadcastMessage("There was no winner this match!");
        tempWinner = "No one";
    }

    public String getOffensive() {
        return "Kill other players!";
    }

    public String getDefensive() {
        return "Don't get yourself killed!";
    }

    public String getFullName() {
        return "Last Team Standing";
    }

    public String getName() {
        return "LTS";
    }

    public String getGrammar() {
        return "an";
    }

    public void onLeave(WarPlayer left) {
        // Do the usual stuff!
        updateScoreboard();
        checkWin();
    }

    public void updateScoreboard() {
        // Get the "objective" on the scoreboard, where data goes.
        Objective obj = s().getObjective(DisplaySlot.SIDEBAR);

        // The title of the scoreboard, which displays the map and gamemode playing this match.
        String dp = map().getMapName() + " (" + getName() + ")";
        if (dp.length() > 32) dp = dp.substring(0, 32); // Titles cannot be longer than 32 characters.
        obj.setDisplayName(dp); // Set the title of the scoreboard.
        obj.setDisplaySlot(DisplaySlot.SIDEBAR); // Ensure it is on the sidebar.

        Iterator<WarTeam> iterator = getTeams().iterator(); // An iterator to iterate through the teams.
        for (int i = 0; i < getTeams().size(); i++) { // Only iterate the number of teams needed.
            // For each team, display their their player count colored respectively.
            WarTeam target = iterator.next(); // Get the next team to be iterated.
            // Set the new score value.
            obj.getScore(target.getTeamColor() + "    " + target.getBukkitTeam().getEntries().size()).setScore(i + 1);
            // Remove the old value from the board since it is not needed.
            s().resetScores(target.getTeamColor() + "    " + (target.getBukkitTeam().getEntries().size() + 1));
        }

        obj.getScore("  ").setScore(0); // Bottom spacer.

    }

    /**
     * Check if there is 1 or less teams with 1
     * or more players remaining. If that is the
     * case, end the round.
     */
    private void checkWin() {
        if (!active) return; // Don't execute this if the gamemode isn't active.
        int remainingTeams = 0;
        for (WarTeam team : getTeams())
            if (team.getBukkitTeam().getEntries().size() >= 1) remainingTeams++; // This team is still alive.

        if (remainingTeams <= 1) // Is there one or less remaining teams?
            onEnd();

    }

    /**
     * Sneaking is a strategy often used to hide
     * on maps, so sneaking will not allow you to
     * hide your name tag behind walls.
     *
     * @param event An event called by Spigot.
     */
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        event.setCancelled(true);
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Participants", original.get(team.getTeamName()));
        return extra;
    }
}
