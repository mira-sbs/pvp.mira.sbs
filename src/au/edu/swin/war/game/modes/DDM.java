package au.edu.swin.war.game.modes;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.Activatable;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * An extension to gamemode to implement DDM.
 * District Death Match objectives have been defined
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
public class DDM extends Gamemode {

    private HashMap<String, Integer> scores; // Keeps the team's scores.

    public void reset() {
        if (scores != null)
            scores.clear();
        scores = null;
    }

    public void initialize() {
        scores = new HashMap<>(); // Initialize key/value set for scores.

        for (WarTeam team : getTeams()) // For every team, assign their scores to 3 x the amount of players online.
            scores.put(team.getTeamName(), Bukkit.getOnlinePlayers().size() * 3);

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
    }

    public void onDeath(WarPlayer killed) {
    }

    public void onLeave(WarPlayer left) {
        //Nothing happens when a player leaves on DDM.
    }

    public String getOffensive() {
        return "Run into the enemy's \"territory\" to score a lot of points!";
    }

    public String getDefensive() {
        return "Stop the enemy from getting into your \"territory\"!";
    }

    public String getFullName() {
        return "District Death Match";
    }

    public String getName() {
        return "DDM";
    }

    public String getGrammar() {
        return "a";
    }

    public void decideWinner() {
        int lowest = 999; // Lowest is higher than 80 x 3 since teams start off as 3 x the amount of players.
        ArrayList<String> winners = new ArrayList<>(); // Keep a temporary list of winners.

        for (WarTeam team : getTeams()) {
            // For each team, check their scores.
            int count = scores.get(team.getTeamName());
            if (count == lowest)
                // If they're equal to the current lowest points, add them to the list of winners.
                winners.add(team.getDisplayName());
            else if (count < lowest) {
                // If they're below the current lowest points,
                // lowezt the new lowest points,
                lowest = count;
                // Clear the current list of winners as they have more points than this team,
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
            Bukkit.broadcastMessage(winner + " is the winner with " + lowest + " run-ins!");
            tempWinner = main.strings().sentenceFormat(winners);
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
        obj.getScore(" ").setScore(scores.size() + 2); // Top spacer.
        obj.getScore("  Run-ins Remaining").setScore(scores.size() + 1); // 'Points' denoter.

        Iterator<WarTeam> iterator = getTeams().iterator(); // An iterator to iterate through the teams.
        for (int i = 0; i < getTeams().size(); i++) { // Only iterate the number of teams needed.
            // For each team, display their their points colored respectively.
            WarTeam target = iterator.next(); // Get the next team to be iterated.
            // Set the new score value.
            obj.getScore(target.getTeamColor() + "    " + scores.get(target.getTeamName())).setScore(i + 1);
            // Remove the old value from the board since it is not needed.
            s().resetScores(target.getTeamColor() + "    " + (scores.get(target.getTeamName()) + 1));
        }
        obj.getScore("  ").setScore(0); // Bottom spacer.
    }

    @Override
    protected HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Remaining Score", scores.get(team.getTeamName()));
        return extra;
    }

    /**
     * A territory is a cuboid, in which if an
     * opposing player runs into, scores a lot
     * of points for their team on DDM. Alongside
     * killing enemy players, they must also
     * protect their territory from being entered.
     */
    public static class Territory implements Listener, Activatable {
        final int x1;
        final int y1;
        final int z1;
        final int x2;
        final int y2;
        final int z2;
        final String belongsTo;
        final WarManager main;

        public Territory(int x1, int y1, int z1, int x2, int y2, int z2, WarTeam belongsTo, WarManager main) {
            // Defines the bottom-left and top-right regions of this cuboid.
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.z1 = Math.min(z1, z2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
            this.z2 = Math.max(z1, z2);
            this.belongsTo = belongsTo.getDisplayName(); // Who does this territory belong to?
            this.main = main;
        }

        /**
         * Awaken this Territory Cuboid for the match.
         */
        public void activate() {
            if (!main.match().getCurrentMode().getFullName().equals("District Death Match"))
                // Don't enable if DDM isn't being played.
                return;

            main.plugin().getServer().getPluginManager().registerEvents(this, main.plugin());
        }

        /**
         * Put this Territory Cuboid to sleep until it is needed again.
         */
        public void deactivate() {
            HandlerList.unregisterAll(this);
        }

        /**
         * Checks if a location is inside the cuboid.
         * This is used to check if a player has entered
         * this territory and needs to be acted upon.
         *
         * @param loc The location to compare.
         * @return Are they inside the territory?
         */
        boolean isInside(Location loc) {
            return loc.getX() >= x1 && loc.getX() <= x2 && loc.getY() >= y1 && loc.getY() <= y2 && loc.getZ() >= z1 && loc.getZ() <= z2;
        }

        @EventHandler
        public void nmv(PlayerMoveEvent event) {
            if (isInside(event.getTo()) && !event.getPlayer().isDead()) { // Are they inside the territory?
                if (!event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) return; // Are they in survival mode?
                WarPlayer wp = main.getWarPlayer(event.getPlayer()); // Get their WarPlayer implement.
                if (wp.getCurrentTeam() == null) return; // Cancel if they aren't on any team.

                WarTeam target = wp.getCurrentTeam(); // Get their current team otherwise!
                if (!target.getDisplayName().equals(belongsTo)) { // Is this not their own territory?
                    DDM ddm = (DDM) main.cache().getGamemode("District Death Match"); // Get the running instance of DDM again.
                    for (WarPlayer wp2 : main.getWarPlayers().values()) { // Loop through each player.
                        if (wp2.getCurrentTeam() == null) continue; // Ignore if they're not on a team.
                        if (!wp2.getCurrentTeam().getTeamName().equals(target.getTeamName())) // Play a bad sound effect.
                            wp2.getPlayer().playSound(wp2.getPlayer().getLocation(), Sound.ENTITY_GHAST_SCREAM, 1F, 1F);
                        else // Play a good sound effect!
                            wp2.getPlayer().playSound(wp2.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
                    }

                    // Broadcast that they got in.
                    Bukkit.broadcastMessage(wp.getTeamName() + " infiltrated " + belongsTo + "");
                    ddm.logEvent(wp.getTeamName() + " infiltrated " + belongsTo + "");

                    // Record their current captures remaining, and decrement it.
                    int capsToGo = ddm.scores.get(target.getTeamName());
                    ddm.scores.put(target.getTeamName(), capsToGo - 1);

                    ddm.updateScoreboard(); // Update the scoreboard.
                    if (capsToGo == 1) // Is this their last capture?
                        ddm.onEnd(); // End the match.
                    else // Other teleport the player back to a random spawn.
                        event.setTo(ddm.map().getTeamSpawns(target.getTeamName()).get(new Random().nextInt(ddm
                                .map().getTeamSpawns(target.getTeamName()).size())).toLocation(main.match().getCurrentWorld(), true));
                } else {
                    event.getPlayer().sendMessage("You're supposed to stop the enemy from getting into here!");
                    event.setTo(event.getFrom());
                }
            }
        }

    }
}