package sbs.mira.pvp.game.modes;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An extension to gamemode to implement LP.
 * Lifepool objectives have been defined
 * in my design brief, so I will assume you
 * know what you are expecting to look at here.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see MiraPulse
 * <p>
 * Created by Josh on 21/04/2017.
 * @since 1.0
 */
public class LP extends Gamemode {

    private final HashMap<String, Integer> lives = new HashMap<>(); // Key/value set to hold teams' lives.

    public void reset() {
        // Clears the "lives" key/value set for next time a LP is played.
        lives.clear();
    }

    public void initialize() {
        for (WarTeam team : getTeams()) // Give every participating team a finite amount of lives.
            lives.put(team.getTeamName(), (Bukkit.getOnlinePlayers().size() * 5) + 3);

        autoAssign();

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
        death(killed);
    }

    public void onDeath(MiraPlayer killed) {
        death(killed);
    }

    /**
     * Procedure that handles death within a round.
     * A life is decremented from the dead player's
     * team's life pool.
     *
     * @param killed Player who died.
     */
    private void death(MiraPlayer killed) {
        int lives = this.lives.get(killed.getCurrentTeam().getTeamName()); // Check their current team's lives.
        if (lives == 0) return; // They've already lost, there's no point continuing.
        this.lives.put(killed.getCurrentTeam().getTeamName(), lives - 1);
        updateScoreboard(); // Reflect the change on the scoreboard.
        checkWin();
    }

    public void decideWinner() {
        int highest = -1; // Highest is lower than zero since teams start off as zero.
        ArrayList<WarTeam> winners = new ArrayList<>(); // Keep a temporary list of winners.

        for (WarTeam team : getTeams()) {
            // For each team, check their kills.
            int count = lives.get(team.getTeamName());
            if (count == highest)
                // If they're equal to the current highest points, add them to the list of winners.
                winners.add(team);
            else if (count > highest) {
                // If they're above the current highest points,
                // Set the new highst points,
                highest = count;
                // Clear the current list of winners as they have less points than this team,
                winners.clear();
                // Then add this team to the list of winners.
                winners.add(team);
            }
        }
        broadcastWinner(winners, "lives remaining", highest);
    }

    private void checkWin() {
        int aliveTeams = 0; // Record how many teams have more than 0 lives remaining.
        for (WarTeam team : getTeams())
            if (lives.get(team.getTeamName()) >= 1)
                aliveTeams++; // This team has more than 0 lives.
        if (aliveTeams <= 1) // Is there one or less teams remaining?
            onEnd();
    }

    public String getOffensive() {
        return "Kill enemies to deplete their lifepool!";
    }

    public String getDefensive() {
        return "Protect your team and your lives!";
    }

    public String getName() {
        return "LP";
    }

    public String getFullName() {
        return "Lifepool";
    }

    public String getGrammar() {
        return "an";
    }

    public void onLeave(MiraPlayer left) {
        //Nothing happens when a player leaves on TDM.
        // Everything is handled automatically. Yay!
    }

    public void updateScoreboard() {
        // Get the "objective" on the scoreboard, where data goes.
        Objective obj = s().getObjective(DisplaySlot.SIDEBAR);

        // The title of the scoreboard, which displays the map and gamemode playing this match.
        String dp = map().getMapName() + " (" + getName() + ")";
        if (dp.length() > 32) dp = dp.substring(0, 32); // Titles cannot be longer than 32 characters.
        obj.setDisplayName(dp); // Set the title of the scoreboard.
        obj.setDisplaySlot(DisplaySlot.SIDEBAR); // Ensure it is on the sidebar.

        // Format it pretty for the players.
        obj.getScore(" ").setScore(lives.size() + 2); // Top spacer.
        obj.getScore("  Lives Remaining").setScore(lives.size() + 1); // 'Points' denoter.

        Iterator<WarTeam> iterator = getTeams().iterator(); // An iterator to iterate through the teams.
        for (int i = 0; i < lives.size(); i++) { // Only iterate the number of teams needed.
            // For each team, display their their points colored respectively.
            WarTeam target = iterator.next(); // Get the next team to be iterated.
            // Set the new score value.
            obj.getScore(target.getTeamColor() + "    " + lives.get(target.getTeamName())).setScore(i + 1);
            // Remove the old value from the board since it is not needed.
            s().resetScores(target.getTeamColor() + "    " + (lives.get(target.getTeamName()) + 1));
        }
        obj.getScore("  ").setScore(0); // Bottom spacer.
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Lives Remaining", lives.get(team.getTeamName()));
        return extra;
    }
}
