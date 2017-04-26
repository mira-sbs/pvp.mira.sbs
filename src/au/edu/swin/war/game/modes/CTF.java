package au.edu.swin.war.game.modes;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.util.Manager;
import au.edu.swin.war.util.WoolColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An extension to gamemode to implement CTF.
 * Capture The Flag objectives have been defined
 * in my design brief, so I will assume you
 * know what you are expecting to look at here.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarManager
 * <p>
 * Created by Josh on 24/04/2017.
 * @since 1.0
 */
public class CTF extends Gamemode {

    private HashMap<String, CTFInfo> info; // Key/vaslue set of CTF Info for each team.
    private HashMap<String, String> capture; // Key/value set of who's holding who's flag.
    private boolean instantBreak; // Whether or not flags break on punching them or not.
    private int interval = 1; // Interval at which fireworks shoot from the flags.

    public void reset() {
        if (capture != null)
            capture.clear();
        capture = null;
        if (info != null)
            info.clear();
        info = null;
        instantBreak = false;
    }

    public void initialize() {
        interval = 1; // Reset the interval if applicable.
        info = new HashMap<>();
        capture = new HashMap<>();

        for (WarTeam team : getTeams()) // Create CTFInfo for each participating team.
            info.put(team.getTeamName(), new CTFInfo(team));

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
        interval--;
        if (interval == 0) {
            // Do fireworks every 4 seconds.
            interval = 4;
            doFireworks();
        }
        // If the amount of time elapsed is 2.5 minutes times the amount of captures required to win,
        if (getTimeElapsed() > (150 * (Integer) map().attr().get("captureRequirement")) && !instantBreak) {
            // Enable "instant break".
            instantBreak = true;
            Bukkit.broadcastMessage("This match is taking too long, Instant Break is now enabled!");
            logEvent("Instant break was enabled!");
        }
    }

    public void onKill(WarPlayer killed, WarPlayer killer) {
        dropFlag(killed);
    }

    public void onDeath(WarPlayer dead) {
        dropFlag(dead);
    }

    /**
     * If the player is holding a flag and they die,
     * drop the flag, restore it, and broadcast it.
     * <p>
     * This is called from both classes since they are
     * functionally identical except one has a killer.
     *
     * @param killed The player who died.
     */
    private void dropFlag(WarPlayer killed) {
        for (CTFInfo inf : info.values()) {
            if (inf.getHolder() != null)
                if (inf.getHolder().equals(killed.getName())) {
                    capture.remove(killed.getName()); // Remove their flagholder status.
                    Bukkit.broadcastMessage(killed.getTeamName() + " dropped " + inf.getTeam().getDisplayName() + "'s flag!"); // Broadcast the change.
                    logEvent(killed.getTeamName() + " dropped " + inf.getTeam().getDisplayName() + "'s flag!"); // Log the change.
                    for (Player target : Bukkit.getOnlinePlayers())
                        target.playSound(target.getLocation(), Sound.ENTITY_IRONGOLEM_HURT, 1F, 1F); // Play a sound effect.
                    inf.setHolder(null); // Set the holder to null.
                    restoreFlags(); // Restore the flag.
                    updateScoreboard(); // Update the scoreboard.
                }
        }
    }

    public void decideWinner() {
        int highest = -1; // Highest is lower than zero since teams start off as zero.
        ArrayList<String> winners = new ArrayList<>(); // Keep a temporary list of winners.

        for (WarTeam team : getTeams()) {
            // For each team, check their kills.
            int count = info.get(team.getTeamName()).getCaptures();
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
            Bukkit.broadcastMessage(winner + " is the winner with " + highest + " captures!");
            tempWinner = main.strings().sentenceFormat(winners);
        }
    }

    public void onLeave(WarPlayer left) {
        dropFlag(left); // Call this because people who aren't in the match can't hold the flag.
    }

    /**
     * Spawns fireworks at each flag or flag holder location.
     */
    private void doFireworks() {
        for (CTFInfo inf : info.values())
            if (inf.getHolder() == null) // Spawn at flag.
                ((Manager) main).entity().spawnFirework(inf.getFlag().clone().add(0.5, 1, 0.5), inf.getTeam().getTeamColor());
            else // Spawn at holder.
                ((Manager) main).entity().spawnFirework(Bukkit.getPlayer(inf.getHolder()).getLocation(), inf.getTeam().getTeamColor());
    }

    /**
     * Restores any flags to their pedestals if
     * they are currently not being held.
     */
    private void restoreFlags() {
        for (CTFInfo inf : info.values())
            if (inf.getHolder() == null) { // If this flag isn't being held...
                Block flag = inf.flag.getBlock(); // Get the flag's block.
                flag.setType(Material.WOOL); // Set it to wool.
                flag.setData(WoolColor.fromChatColor(inf.target.getTeamColor()).getColor()); // Color the wool.
            }
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
        obj.getScore(" ").setScore(info.size() + 2); // Top spacer.
        obj.getScore("  Captures").setScore(info.size() + 1); // 'Points' denoter.

        int rqmt = (int) map().attr().get("captureRequirement"); // The amount of captures required to win.
        Iterator<WarTeam> iterator = getTeams().iterator(); // An iterator to iterate through the teams.
        for (int i = 0; i < info.size(); i++) { // Loop through each CTFInfo in the array.
            CTFInfo inf = info.get(iterator.next().getTeamName());
            if (inf.getHolder() == null) {
                // Are they holding the flag?
                // If so, reset all other states the scoreboard can be in.
                // Then, write the state the scoreboard is in.
                obj.getScore(inf.getTeam().getTeamColor() + "    █ " + inf.getCaptures() + ChatColor.GRAY + "/" + rqmt).setScore(i + 1);
                s().resetScores(inf.getTeam().getTeamColor() + "    ▓ " + inf.getCaptures() + ChatColor.GRAY + "/" + rqmt);
                s().resetScores(inf.getTeam().getTeamColor() + "    █ " + (inf.getCaptures() - 1) + ChatColor.GRAY + "/" + rqmt);
                s().resetScores(inf.getTeam().getTeamColor() + "    ▓ " + (inf.getCaptures() - 1) + ChatColor.GRAY + "/" + rqmt);
            } else {
                // If someone is holding this flag, display it as stolen on the scoreboard.
                // Reset the non-stolen state on the scoreboard.
                obj.getScore(inf.getTeam().getTeamColor() + "    ▓ " + inf.getCaptures() + ChatColor.GRAY + "/" + rqmt).setScore(i + 1);
                s().resetScores(inf.getTeam().getTeamColor() + "    █ " + inf.getCaptures() + ChatColor.GRAY + "/" + rqmt);
            }
        }
        obj.getScore("  ").setScore(0); // Bottom spacer.
    }


    public String getOffensive() {
        return "Steal the other enemy's flag and capture it by punching your flag!";
    }

    public String getDefensive() {
        return "Stop the enemy from taking your flag!";
    }

    public String getFullName() {
        return "Capture The Flag";
    }

    public String getName() {
        return "CTF";
    }

    public String getGrammar() {
        return "a";
    }

    /**
     * Check if a win has been attained after a capture.
     * If there is a win, onEnd should be called.
     *
     * @return Whether or not any team has won.
     */
    private boolean checkWin() {
        for (CTFInfo inf : info.values())
            if (inf.getCaptures() >= (Integer) map().attr().get("captureRequirement"))
                return true;
        return false;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BEDROCK) return;
        WarPlayer wp = main.getWarPlayer(event.getPlayer());
        event.setCancelled(checkBreak(wp, event.getBlock())); // Depending on the value the function returns, cancel the block breaking.
    }

    @EventHandler
    public void onPunch(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        WarPlayer wp = main.getWarPlayer(event.getPlayer().getUniqueId());
        if (capture.containsKey(wp.getName()))
            for (CTFInfo inf : info.values()) { // For every team's flag...
                if (inf.flag.equals(event.getClickedBlock().getLocation())) { // Did they click a flag?
                    if (inf.target.getTeamName().equals(wp.getCurrentTeam().getTeamName())) { // Is it their team's flag?
                        if (inf.getHolder() == null) {

                            for (Player target : Bukkit.getOnlinePlayers())
                                target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F); // Play a sound effect.
                            capture.remove(wp.getName()); // They are no longer holding the flag if they captured it.
                            inf.addCapture(); // Increment their capture count.
                            for (CTFInfo inf2 : info.values()) // Loop through every team's flag again...
                                if (inf2.getHolder() != null) // Is someone holding their flag?
                                    if (inf2.getHolder().equals(wp.getName())) { // Was it their flag that just got captured?
                                        // If no one is holding their flag, capture it!
                                        Bukkit.broadcastMessage(wp.getTeamName() + " captured " + inf2.getTeam().getDisplayName() + "'s flag!");
                                        logEvent(wp.getTeamName() + " captured " + inf2.getTeam().getDisplayName() + "'s flag"); // Log the capture.
                                        inf2.setHolder(null); // No one is holding their flag anymore as it got captured.
                                        break;
                                    }
                            restoreFlags(); // Restore the flags.
                            updateScoreboard(); // Update the scoreboard.
                            if (checkWin()) { // Was this a winning capture?
                                onEnd();
                                break;
                            }
                        } else
                            wp.sendMessage(inf.getHolder() + " is holding your team's flag. You cannot capture!");
                    }
                }
            }
        else if (instantBreak) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                checkBreak(wp, event.getClickedBlock());
            }
        }
    }

    /**
     * Checks if a block broken was a flag.
     * This also applies to instant capture mode.
     *
     * @param wp     The player who broke a block.
     * @param broken The block broken.
     * @return Whether the event needs to be cancelled or not.
     */
    private boolean checkBreak(WarPlayer wp, Block broken) {
        for (CTFInfo inf : info.values()) { // For every team's flag..
            if (inf.flag.equals(broken.getLocation())) { // Did they break a flag?
                if (capture.containsKey(wp.getName())) {
                    // Are they already holding a flag?
                    wp.sendMessage("You can't steal more than one flag at once!");
                    return true;
                }
                if (wp.getCurrentTeam().getTeamName().equals(inf.target.getTeamName())) {
                    // Are they trying to steal their own flag?
                    wp.sendMessage("You can't steal your own flag! Defend it!");
                    return true;
                }
                // Otherwise,
                inf.setHolder(wp.getName()); // Set the holder as this player.

                capture.put(wp.getName(), inf.target.getTeamColor() + inf.target.getTeamName()); // Register the player as a flag holder.
                info.get(wp.getCurrentTeam().getTeamName()).addAttempt(); // Record the steal as an attempt.

                Bukkit.broadcastMessage(wp.getTeamName() + " has stolen " + inf.getTeam().getDisplayName() + "'s flag!"); // Broadcast it!
                logEvent(wp.getTeamName() + " has stolen " + inf.getTeam().getDisplayName() + "'s flag"); // Log the steal.

                for (Player target : Bukkit.getOnlinePlayers())
                    target.playSound(target.getLocation(), Sound.ENTITY_ARROW_HIT, 1F, 1F); // Play a sound effect.
                broken.setType(Material.BEDROCK); // Turn it to bedrock so it can't be broken.
                updateScoreboard(); // Update the scoreboard.
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.WOOL) {
            // Don't allow wool to be picked up.
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    /**
     * Private record to hold a list of CTF information for a team.
     * This class holds:
     * -> The team associated with it.
     * -> The location of their flag.
     * -> The holder of their flag, if any.
     * -> The amount of captures they've made.
     * -> The amount of flag steals they've made.
     */
    private class CTFInfo {
        final WarTeam target;
        final Location flag;
        String holder;
        int captures;
        int attempts;

        CTFInfo(WarTeam target) {
            this.target = target;
            flag = ((HashMap<String, SerializedLocation>) map().attr().get("flags")).get(target.getTeamName()).toLocation(main.match().getCurrentWorld(), false);
            holder = null;
            captures = 0;
            attempts = 0;
        }

        void addCapture() {
            captures++;
        }

        int getCaptures() {
            return captures;
        }

        void addAttempt() {
            attempts++;
        }

        int getAttempts() {
            return attempts;
        }

        WarTeam getTeam() {
            return target;
        }

        String getHolder() {
            return holder;
        }

        void setHolder(String holder) {
            this.holder = holder;
        }

        Location getFlag() {
            return flag;
        }
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Flag Captures", info.get(team.getTeamName()).getCaptures());
        extra.put("Flag Steals", info.get(team.getTeamName()).getAttempts());
        return extra;
    }
}