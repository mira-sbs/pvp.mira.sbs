package au.edu.swin.war.game.modes;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An extension to gamemode to implement TDM.
 * Team Death Match objectives have been defined
 * in my design brief, so I will assume you
 * know what you are expecting to look at here.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarManager
 * <p>
 * Created by Josh on 21/04/2017.
 * @since 1.0
 */
public class TDM extends Gamemode {

    private final HashMap<String, Integer> kills = new HashMap<>(); // Key/value set to hold teams' points.

    public void reset() {
        // Clears the "kills" key/value set for next time a TDM is played.
        kills.clear();
    }

    public void initialize() {
        for (WarTeam team : getTeams()) // Give every participating team a default score of zero.
            kills.put(team.getTeamName(), 0);

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

    public void onKill(WarPlayer killed, WarPlayer killer) {
        // Increments the killer's team's points.
        kills.put(killer.getCurrentTeam().getTeamName(), kills.get(killer.getCurrentTeam().getTeamName()) + 1);
        updateScoreboard(); // Update the scoreboard to reflect the change.
    }

    public void onDeath(WarPlayer killed) {
        // If the player kills themselves, award a point to every opposition team.
        for (WarTeam awarded : getTeams()) {
            if (!awarded.getTeamName().equals(killed.getCurrentTeam().getTeamName())) // Is this team not their team?
                kills.put(awarded.getTeamName(), kills.get(awarded.getTeamName()) + 1); // Increment their points!
        }
        updateScoreboard(); // Update the scoreboard to reflect the change.
    }

    public void decideWinner() {
        int highest = -1; // Highest is lower than zero since teams start off as zero.
        ArrayList<String> winners = new ArrayList<>(); // Keep a temporary list of winners.

        for (WarTeam team : getTeams()) {
            // For each team, check their kills.
            int count = kills.get(team.getTeamName());
            if (count == highest)
                // If they're equal to the current highest points, add them to the list of winners.
                winners.add(team.getDisplayName());
            else if (count > highest) {
                // If they're above the current highest points,
                // Set the new highst points,
                highest = count;
                // Clear the current list of winners as they have less points than this team,
                winners.clear();
                // Then add this team to the list of winners.
                winners.add(team.getDisplayName());
            }
        }

        // Is there more than one winner?
        if (winners.size() > 1) {
            Bukkit.broadcastMessage("It's a " + winners.size() + "-way tie! " + main.strings().sentenceFormat(winners) + " tied!");
            tempWinner = main.strings().sentenceFormat(winners);
        } else if (winners.size() == 1) {
            String winner = winners.get(0); // Get the singleton winner!
            // ChatColor.stripColor() is used to remove the team's color from the String so it can be queried to get their points.
            Bukkit.broadcastMessage(winner + ChatColor.WHITE + " is the winner with " + highest + " points!");
            tempWinner = main.strings().sentenceFormat(winners);
        }
    }

    public String getOffensive() {
        return "Kill players to score points!";
    }

    public String getDefensive() {
        return "Don't let the enemy kill you! They will get points!";
    }

    public String getName() {
        return "TDM";
    }

    public String getFullName() {
        return "Team Death Match";
    }

    public String getGrammar() {
        return "a";
    }

    public void onLeave(WarPlayer left) {
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
        obj.getScore(" ").setScore(kills.size() + 2); // Top spacer.
        obj.getScore("  Points").setScore(kills.size() + 1); // 'Points' denoter.

        Iterator<WarTeam> iterator = getTeams().iterator(); // An iterator to iterate through the teams.
        for (int i = 0; i < kills.size(); i++) { // Only iterate the number of teams needed.
            // For each team, display their their points colored respectively.
            WarTeam target = iterator.next(); // Get the next team to be iterated.
            // Set the new score value.
            obj.getScore(target.getTeamColor() + "    " + kills.get(target.getTeamName())).setScore(i + 1);
            // Remove the old value from the board since it is not needed.
            s().resetScores(target.getTeamColor() + "    " + (kills.get(target.getTeamName()) - 1));
        }
        obj.getScore("  ").setScore(0); // Bottom spacer.
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Points", kills.get(team.getTeamName()));
        return extra;
    }
}
