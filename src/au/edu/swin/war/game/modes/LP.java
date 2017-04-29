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
 * An extension to gamemode to implement LP.
 * Lifepool objectives have been defined
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
public class LP extends Gamemode {

    private final HashMap<String, Integer> lives = new HashMap<>(); // Key/value set to hold teams' lives.

    public void reset() {
        // Clears the "lives" key/value set for next time a LP is played.
        lives.clear();
    }

    public void initialize() {
        for (WarTeam team : getTeams()) // Give every participating team a finite amount of lives.
            lives.put(team.getTeamName(), Bukkit.getOnlinePlayers().size() * 5);

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

    public void tick() {
        //Nothing needed here.
    }

    public void onKill(WarPlayer killed, WarPlayer killer) {
        death(killed);
    }

    public void onDeath(WarPlayer killed) {
        death(killed);
    }

    /**
     * Procedure that handles death within a round.
     * A life is decremented from the dead player's
     * team's life pool.
     *
     * @param killed Player who died.
     */
    private void death(WarPlayer killed) {
        int lives = this.lives.get(killed.getCurrentTeam().getTeamName()); // Check their current team's lives.
        if (lives == 0) return; // They've already lost, there's no point continuing.
        this.lives.put(killed.getCurrentTeam().getTeamName(), lives - 1);
        updateScoreboard(); // Reflect the change on the scoreboard.
        checkWin();
    }

    public void decideWinner() {
        int highest = -1; // Highest is lower than zero since teams start off as zero.
        ArrayList<String> winners = new ArrayList<>(); // Keep a temporary list of winners.

        for (WarTeam team : getTeams()) {
            // For each team, check their kills.
            int count = lives.get(team.getTeamName());
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
            Bukkit.broadcastMessage(winner + ChatColor.WHITE + " is the winner with " + highest + " lives remaining!");
            tempWinner = main.strings().sentenceFormat(winners);
        }
    }

    private void checkWin() {
        int aliveTeams = 0; // Record how many teams have more than 0 lives remaining.
        for (WarTeam team : getTeams())
            if (lives.get(team.getTeamName()) >= 0)
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
