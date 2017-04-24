package au.edu.swin.war.game.modes;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.util.Manager;
import au.edu.swin.war.util.WoolColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An extension to gamemode to implement KoTH.
 * King of The Hill objectives have been defined
 * in my design brief, so I will assume you
 * know what you are expecting to look at here.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarManager
 * <p>
 * Created by Josh on 23/04/2017.
 * @since 1.0
 */
public class KoTH extends Gamemode {

    private WarTeam holder; // The current team holding the flag. Null if there is no team.
    private HashMap<String, Integer> captureTime; // The capture time remaining for each team.
    private HashMap<String, Integer> captures; // The amount of captures made by each team.
    private Location flag; // The block location of the flag.
    private int interval; // Interval at which fireworks shoot from the flag.

    public void reset() {
        flag = null;
        holder = null;
        if (captureTime != null)
            captureTime.clear();
        captureTime = null;
    }

    public void initialize() {
        interval = 1; // Reset the interval if applicable.
        captureTime = new HashMap<>();
        captures = new HashMap<>();
        flag = ((SerializedLocation) map().attr().get("kothFlag")).toLocation(main.match().getCurrentWorld(), false);

        for (WarTeam team : getTeams()) { // Give every participating team default values.
            captureTime.put(team.getTeamName(), 180);
            captures.put(team.getTeamName(), 0);
        }

        // Defines the block at which the flag is located.
        main.match().getCurrentWorld().getBlockAt(flag).setType(Material.WOOL);

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
        // This is all called every second.
        if (holder != null) {
            int holdTime = captureTime.get(holder.getTeamName()); // Get the team's current hold time.
            captureTime.put(holder.getTeamName(), holdTime - 1); // Subtract by 1 and push back into key/value set.
            updateScoreboard(); // Update the scoreboard!
            holdTime--; // Subtract the holdTime field we just made.
            if (holdTime == 5) {
                logEvent(holder.getDisplayName() + " will win in 5 seconds!"); // Log event.
                Bukkit.broadcastMessage(holder.getDisplayName() + " will win in 5 seconds!"); // They're gonna win!
            } else if (holdTime == 0) {
                // They won.
                onEnd();
                return;
            }
        }
        interval--;
        if (interval == 0) {
            // Do a firework every 4 seconds.
            interval = 4;
            doFireworks();
        }
    }

    public void onKill(WarPlayer killed, WarPlayer killer) {
        // Unneeded in this gamemode.
    }

    public void onLeave(WarPlayer left) {
        // Unneeded in this gamemode.
    }

    public void onDeath(WarPlayer killed) {
        // Unneeded in this gamemode.
    }

    public void decideWinner() {
        int lowest = 999; // Lowest is higher than the initial time.
        ArrayList<String> winners = new ArrayList<>(); // Keep a temporary list of winners.

        for (WarTeam team : getTeams()) {
            // For each team, check their capture time.
            int time = captureTime.get(team.getTeamName());
            if (time == lowest)
                // If they're equal to the current lowest time, add them to the list of winners.
                winners.add(team.getDisplayName());
            else if (time < lowest) {
                // If they're above the current lowest time,
                // Set the new lowest time,
                lowest = time;
                // Clear the current list of winners as they have more time than this team,
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
            Bukkit.broadcastMessage(winner + " is the winner!");
            tempWinner = main.strings().sentenceFormat(winners);
        }
    }

    /**
     * KoTH-specific procedure to spawn a firework at the flag.
     * If no one is holding it, spawn a white firework.
     * If a team is holding it, spawn a holding-team-colored firework.
     */
    private void doFireworks() {
        if (holder == null) // Spawn white.
            ((Manager) main).entity().spawnFirework(flag.clone().add(0.5, 1, 0.5), ChatColor.WHITE);
        else // Spawn team-colored.
            ((Manager) main).entity().spawnFirework(flag.clone().add(0.5, 1, 0.5), holder.getTeamColor());
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
        obj.getScore(" ").setScore(captureTime.size() + 2); // Top spacer.
        obj.getScore("  Time Remaining").setScore(captureTime.size() + 1); // 'Points' denoter.

        Iterator<WarTeam> iterator = getTeams().iterator(); // An iterator to iterate through the teams.
        for (int i = 0; i < captureTime.size(); i++) { // Only iterate the number of teams needed.
            // For each team, display their their times colored respectively.
            WarTeam target = iterator.next(); // Get the next team to be iterated.
            // Set the new score value.
            obj.getScore(target.getTeamColor() + "    " + main.strings().getDigitalTime(captureTime.get(target.getTeamName()))).setScore(i + 1);
            // Remove the old value from the board since it is not needed.
            s().resetScores(target.getTeamColor() + "    " + main.strings().getDigitalTime(captureTime.get(target.getTeamName()) + 1));
        }
        obj.getScore("  ").setScore(0); // Bottom spacer.
    }

    public String getOffensive() {
        return "Break the wool in the middle of the map to control the flag!";
    }

    public String getDefensive() {
        return "Stop the enemy from controlling the flag if you have control!";
    }

    public String getFullName() {
        return "King of The Hill";
    }

    public String getName() {
        return "KoTH";
    }

    public String getGrammar() {
        return "a";
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        WarPlayer wp = main.getWarPlayer(event.getPlayer()); // Get their WarPlayer implement.
        if (event.getBlock().getLocation().equals(flag)) { // Did they berak the flag?
            event.setCancelled(true); // Cancel the breaking of the flag.
            WarTeam target = wp.getCurrentTeam(); // Get their team.
            if (target == null) return; // Was it a spectator?
            if (holder == target) // Are they already in control of the flag?
                wp.getPlayer().sendMessage("You already have control of the flag!");
            else {
                Bukkit.broadcastMessage(wp.getTeamName() + " took the flag for " + target.getDisplayName() + "!");
                // Log first capture and additional captures!
                if (holder == null)
                    logEvent(wp.getTeamName() + " captured the flag first!");
                else
                    logEvent(wp.getTeamName() + " captured the flag for " + target.getDisplayName());
                holder = target; // Broadcast the taking of the flag and reflect the change.

                for (Player online : Bukkit.getOnlinePlayers())
                    online.playSound(online.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 1F, 1F); // Play a sound effect.
                updateScoreboard(); // Update the scoreboard.
                captures.put(target.getTeamName(), captures.get(target.getTeamName()) + 1); // Increment captures.

                // Update the wool.
                event.getBlock().getLocation().getBlock().setType(Material.WOOL);
                event.getBlock().getLocation().getBlock().setData(WoolColor.fromChatColor(target.getTeamColor()).getColor());
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        // Don't allow the flag to be exploded.
        event.blockList().remove(flag.getBlock());
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.WOOL) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(String teamName) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Capture Time", main.strings().getDigitalTime(captureTime.get(teamName)));
        extra.put("Flag Captures", captures.get(teamName));
        return extra;
    }
}