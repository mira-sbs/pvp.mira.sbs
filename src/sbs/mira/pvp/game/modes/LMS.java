package sbs.mira.pvp.game.modes;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * An extension to gamemode to implement LMS.
 * Last Man Standing objectives have been defined
 * in my design brief, so I will assume you
 * know what you are expecting to look at here.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see MiraPulse
 * <p>
 * Created by Josh on 26/04/2017.
 * @since 1.0
 */
public class LMS extends Gamemode {

    private ArrayList<UUID> participated; // Keep a list of people who participated.
    private ArrayList<UUID> alive; // Who is currently alive in the match?

    public void reset() {
        if (participated != null)
            while (participated.size() > 0) {
                MiraPlayer wp = main.getWarPlayer(participated.get(0));
                if (wp != null)
                    wp.setJoined(true); // Re-set this player as joined, since they participated.
                participated.remove(participated.get(0)); // We can remove this player from the list now.
            }
        participated = null; // Remove instance of this list.

        // Clear and reset the list of alive players.
        if (alive != null)
            alive.clear();
        alive = null;
    }

    public void initialize() {
        // Initialize array lists first!
        alive = new ArrayList<>();
        participated = new ArrayList<>();

        if (getJoined() < 2) {
            // LMS requires 2 players at the least to play.
            Bukkit.broadcastMessage("There needs to be 2 or more participating players!");
            logEvent("Match cancelled as there was not enough players");
            onEnd();
            return;
        }

        for (WarTeam team : getTeams()) // Since this is LMS, allow friendly fire.
            team.getBukkitTeam().setAllowFriendlyFire(true);

        autoAssign();

        // Mark playing players are participated.
        for (MiraPlayer check : main.getWarPlayers().values())
            if (check.is_member_of_team()) {
                alive.add(check.crafter().getUniqueId());
                participated.add(check.crafter().getUniqueId());
            }

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

    public void onKill(MiraPlayer killed, MiraPlayer killer) {
        dead(killed);
    }

    public void onDeath(MiraPlayer dead) {
        dead(dead);
    }

    /**
     * Common code is shared by onKill and onDeath,
     * both call to this procedure to prevent duplication.
     *
     * @param dead The player who died.
     */
    private void dead(MiraPlayer dead) {
        // Remove their state as 'alive'.
        alive.remove(dead.crafter().getUniqueId());

        // Update scoreboard
        updateScoreboard();

        // Kick them out of the match as this is permanent death.
        dead.setJoined(false);
        entryHandle(dead);

        checkWin();
    }

    public void decideWinner() {
        if (alive.size() == 1) {
            MiraPlayer winner = main.getWarPlayer(alive.get(0)); // Get the only player in the array.
            if (winner != null) {
                tempWinner = winner.display_name();
                Bukkit.broadcastMessage(winner.display_name() + " is the last man standing!");
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
        return "Last Man Standing";
    }

    public String getName() {
        return "LMS";
    }

    public String getGrammar() {
        return "an";
    }

    public void onLeave(MiraPlayer left) {
        // If they died, we should not re-remove them as this is pointless.
        // They are forced to leave if they die.
        if (!alive.contains(left.crafter().getUniqueId())) return;

        alive.remove(left.crafter().getUniqueId()); // They left, so they're no longer alive.

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

        obj.getScore(" ").setScore(3); // Top spacer.
        obj.getScore("  Still Standing").setScore(2); // A label!
        obj.getScore("    " + alive.size() + "/" + participated.size()).setScore(1); // The amount of standing players!
        obj.getScore("  ").setScore(0); // Bottom spacer.
        s().resetScores("    " + (alive.size() + 1) + "/" + participated.size()); // Reset old score.

    }

    /**
     * If there is 1 or less players remaining,
     * the match is over since it is a last man
     * standing match.
     */
    private void checkWin() {
        if (alive.size() <= 1 && active)  // Make sure this can only be called once a true round ends.
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
        extra.put("Participants", participated.size());
        return extra;
    }
}
