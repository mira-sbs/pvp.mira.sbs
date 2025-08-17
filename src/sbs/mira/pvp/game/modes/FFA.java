package sbs.mira.pvp.game.modes;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An extension to gamemode to implement FFA.
 * Free For All objectives have been defined
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
public class FFA extends Gamemode {

    private HashMap<UUID, Integer> kills;
    private int leadKills;
    private UUID leader;

    public void reset() {
        kills.clear();
        kills = null;
    }

    public void initialize() {
        kills = new HashMap<>();

        for (WarTeam team : getTeams()) // Since this is FFA, allow friendly fire.
            team.getBukkitTeam().setAllowFriendlyFire(true);

        leadKills = 0; // Set the leader's kills to 0.

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
        if (kills.containsKey(killer.crafter().getUniqueId()))
            kills.put(killer.crafter().getUniqueId(), kills.get(killer.crafter().getUniqueId()) + 1); // Increment their amount of kills.
        else
            kills.put(killer.crafter().getUniqueId(), 1); // Otherwise give them a starter count of 1.
        int cKills = kills.get(killer.crafter().getUniqueId());
        if (cKills > leadKills) {
            leadKills = cKills; // This is now the highest amount of kills.
            if (!killer.crafter().getUniqueId().equals(leader)) { // Is this not the same leader?
                leader = killer.crafter().getUniqueId(); // New leader!
                // Broadcast and log that a new leader has taken over.
                Bukkit.broadcastMessage(killer.display_name() + " is now the leader");
                logEvent(killer.display_name() + " is now the leader");
            }
        }
        killer.dm("You now have " + cKills + "/" + getFFAKills() + " kills");
        updateScoreboard();
        checkWin(killer.crafter().getUniqueId());
    }

    public void onDeath(MiraPlayer dead) {
        // Nothing happens when a player dies independent of a killer on FFA.
    }

    public void decideWinner() {
        int highest = -1; // Highest is lower than zero since teams start off as zero.
        ArrayList<String> winners = new ArrayList<>(); // Keep a temporary list of winners.

        for (Map.Entry<UUID, Integer> entry : kills.entrySet()) {
            MiraPlayer found = main.getWarPlayer(entry.getKey()); // Get their WarPlayer implement.
            if (found == null) continue; // If they're not online, they aren't counted here.
            // For each player, check their kills.
            int count = entry.getValue();
            if (count == highest)
                // If they're equal to the current highest points, add them to the list of winners.
                winners.add(found.display_name());
            else if (count > highest) {
                // If they're above the current highest points,
                // Set the new highst points,
                highest = count;
                // Clear the current list of winners as they have less points than this player,
                winners.clear();
                // Then add this player to the list of winners.
                winners.add(found.display_name());
            }
        }

        // Is there more than one winner?
        if (winners.size() > 1) {
            Bukkit.broadcastMessage("It's a " + winners.size() + "-way tie! " + main.strings().sentenceFormat(winners) + " tied!");
            tempWinner = main.strings().sentenceFormat(winners);
        } else if (winners.size() == 1) {
            String winner = winners.get(0); // Get the singleton winner!
            Bukkit.broadcastMessage(winner + ChatColor.WHITE + " is the winner with " + highest + " points!");
            tempWinner = main.strings().sentenceFormat(winners);
        }
    }

    public String getOffensive() {
        return "Kill players to score points for yourself!";
    }

    public String getDefensive() {
        return "Don't let other players kill you!";
    }

    public String getFullName() {
        return "Free For All";
    }

    public String getName() {
        return "FFA";
    }

    public String getGrammar() {
        return "an";
    }

    public void onLeave(MiraPlayer left) {
        //Nothing happens when a player leaves on FFA.
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
        obj.getScore("  Leader's Kills").setScore(2); // A label!
        obj.getScore("    " + leadKills + "/" + getFFAKills()).setScore(1); // The leader's kills.
        obj.getScore("  ").setScore(0); // Bottom spacer.
        s().resetScores("    " + (leadKills - 1) + "/" + getFFAKills()); // Reset old score.

    }

    /**
     * If the player reaches the kill cap, this
     * procedure will automatically end the round.
     *
     * @param player Player to check.
     */
    private void checkWin(UUID player) {
        if (kills.get(player) >= getFFAKills())
            onEnd();
    }

    /**
     * Returns the map's defined score cap for FFA.
     * By default, this score cap is set to 20.
     *
     * @return FFA score cap.
     */
    private Integer getFFAKills() {
        return (Integer) map().attr().get("ffaKills");
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        // FFAs do not contain any additional team data to display.
        return new HashMap<>();
    }
}
