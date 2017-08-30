package au.edu.swin.war.game.modes;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.Activatable;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.*;

/**
 * An extension to gamemode to implement DTM.
 * Destroy The Monument objectives have been defined
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
public class DTM extends Gamemode {

    private List<Monument> monuments; // Keeps a temporary list of the monuments applicable for this match.

    public void reset() {
        monuments.clear();
        monuments = null;
    }

    public void initialize() {
        monuments = new ArrayList<>(); // Keep an array list of the monuments.
        for (Activatable objective : map().objectives())
            if (objective instanceof Monument)
                monuments.add((Monument) objective);

        autoAssign();

        Objective obj = s().registerNewObjective("gm", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateScoreboard();

        for (Player online : Bukkit.getOnlinePlayers())
            online.setScoreboard(s());
    }

    public void tick() {
        //Nothing needed here.
    }

    public void onKill(WarPlayer killed, WarPlayer killer) {
    }

    public void onDeath(WarPlayer dead) {
    }

    public void decideWinner() {
        int highest = -1; // Highest is lower than zero since teams start off as zero.
        ArrayList<WarTeam> winners = new ArrayList<>(); // Keep a temporary list of winners.

        for (Monument mon : getMonuments()) {
            // For each monument, check their damage.
            int count = mon.calculatePercentage(0);
            if (count == highest)
                // If they're equal to the current least damage, add them to the list of winners.
                winners.add(mon.owner);
            else if (count > highest) {
                // If they're above the current least damage,
                // Set the new least damage,
                highest = count;
                // Clear the current list of winners as they have more damage than this team,
                winners.clear();
                // Then add this team to the list of winners.
                winners.add(mon.owner);
            }
        }
        broadcastWinner(winners, "% of their monument remaining", highest);
    }

    /**
     * Checks if one or less monuments are remaining.
     * If true, end the round and decide the winner.
     *
     * @return Whether or not someone has won.
     */
    private boolean checkWin() {
        int monuments = 0;
        for (Monument target : getMonuments())
            if (!target.isDestroyed()) monuments++;
        return monuments <= 1;
    }

    public String getOffensive() {
        return "Reduce enemy monument percentage to 0%!";
    }

    public String getDefensive() {
        return "Protect your own monument!";
    }

    public String getFullName() {
        return "Destroy The Monument";
    }

    public String getName() {
        return "DTM";
    }

    public String getGrammar() {
        return "a";
    }

    public void onLeave(WarPlayer left) {
        //Nothing happens when a player leaves on DTM.
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
        obj.getScore(" ").setScore(getMonuments().size() + 2); // Top spacer.
        obj.getScore("  Monuments").setScore(getMonuments().size() + 1); // 'Points' denoter.

        Iterator<Monument> iterator = getMonuments().iterator(); // An iterator to iterate through the monuments.
        for (int i = 0; i < getTeams().size(); i++) { // Only iterate the number of teams needed.
            // For each monument, display their their damage colored respectively.
            Monument target = iterator.next(); // Get the next monument to be iterated.
            // Set the new score value.
            int calc = target.calculatePercentage(0); // Calculate the percent remaining.
            obj.getScore(target.owner.getTeamColor() + "    " + calc + "%").setScore(i + 1);
            // Remove the old value from the board since it is not needed.
            if (calc < 100) // Only reset it if it is below 100%.
                s().resetScores(target.owner.getTeamColor() + "    " + target.calculatePercentage(1) + "%");
        }
        obj.getScore("  ").setScore(0); // Bottom spacer.
    }

    /**
     * Returns the map's list of monuments.
     *
     * @return List of monuments.
     */
    private List<Monument> getMonuments() {
        return monuments;
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        for (Monument mon : getMonuments()) { // We need to find the monument associated with the team...
            if (!mon.owner.equals(team.getDisplayName())) continue; // It's not this team...
            List<String> footprint = new ArrayList<>(); // Keep a list of the footprint to format...
            for (Map.Entry<UUID, Integer> entry : mon.footprint.entrySet()) {
                int contribution = Math.abs(Math.round((entry.getValue() * 100) / mon.origSize)); // Their contribution to the destruction.
                WarPlayer wp = main.getWarPlayer(entry.getKey()); // Get their WarPlayer implement.
                if (wp != null)
                    footprint.add(wp.getTeamName() + " (" + contribution + "%)");
                else
                    footprint.add(Bukkit.getOfflinePlayer(entry.getKey()).getName() + " (" + contribution + "%)");
            }
            extra.put("Monument State", mon.calculatePercentage(0) + "%"); // Push the state of their monument.
            if (footprint.size() != 0) // If applicable, push the footprint of the monument too.
                extra.put("Damagers", main.strings().sentenceFormat(footprint));
            break;
        }
        return extra;
    }

    /**
     * This class fully defines and operates a Monument.
     * It can calculate the current damage to itself, the
     * footprint of its damage, and the region in which
     * can be broken further.
     *
     * @since 1.0
     */
    public static class Monument implements Listener, Activatable {
        final int x1;
        final int y1;
        final int z1; // Bottom left applicable coordinates.
        final int x2;
        final int y2;
        final int z2; // Top right applicable coordinates.
        final Material composure; // What blocks are this monument made of?
        final WarTeam owner; // What team owns this monument?
        final List<Block> region = new ArrayList<>(); // The blocks associated with this monument.
        final HashMap<UUID, Integer> footprint; // Track who's broken what amount of this monument.
        final WarManager main; // A running instance of the WarManager class.
        int origSize; // The original size of the monument.
        int blocksBroken; // The amount of blocks broken off the monument.
        boolean destroyed = false; // Is this monument destroyed?

        public Monument(int x1, int y1, int z1, int x2, int y2, int z2, WarTeam owner, Material composure, WarManager main) {
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.z1 = Math.min(z1, z2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
            this.z2 = Math.max(z1, z2);
            this.owner = owner;
            this.composure = composure;
            this.footprint = new HashMap<>();
            this.main = main;
        }

        /**
         * Awaken this Monument for the round.
         */
        public void activate() {
            if (!main.match().getCurrentMode().getFullName().equals("Destroy The Monument")) {
                // Remove the monument if DTM is not being played on a map that supports monuments.
                for (Block bl : getBlocks())
                    bl.setType(Material.AIR);
                return;
            }

            // Calculate the monument region and activate listeners.
            region.addAll(getBlocks());
            main.plugin().getServer().getPluginManager().registerEvents(this, main.plugin());
            origSize = region.size();
            blocksBroken = 0;
        }

        /**
         * Put this Monument to sleep until it is needed again.
         */
        public void deactivate() {
            HandlerList.unregisterAll(this);
            region.clear();
            footprint.clear();
            origSize = 0;
            blocksBroken = 0;
            destroyed = false;
        }

        /**
         * Calculates current percentage of monument remaining.
         *
         * @param diff Use this parameter to calculate a previous percentage.
         * @return The remaining percent.
         */
        int calculatePercentage(int diff) {
            int per = blocksBroken - diff;
            int result = -1;
            if (per == 0) result = 100;
            if (result == -1) result = Math.abs(Math.round((per * 100) / origSize) - 100);
            if (per == 1) result = 100 - (result / origSize);
            return result;
        }

        boolean isDestroyed() {
            return destroyed;
        }

        void destroy() {
            destroyed = true;
        }

        /**
         * Checks if a location is inside the cuboid.
         * This is used to check if a block has been
         * broken inside this monument and needs to
         * be checked.
         *
         * @param loc The location to compare.
         * @return Is it inside the monument?
         */
        boolean isInside(Location loc) {
            return loc.getX() >= x1 && loc.getX() <= x2 && loc.getY() >= y1 && loc.getY() <= y2 && loc.getZ() >= z1 && loc.getZ() <= z2;
        }

        /**
         * Get the blocks associated with this monument.
         * Loops through x,y,z from bottom left to top
         * right to get the composure blocks.
         *
         * @return The monument region.
         */
        ArrayList<Block> getBlocks() {
            ArrayList<Block> blocks = new ArrayList<>();
            // For x,y,z... (a 3d cuboid)
            for (int x = this.x1; x <= this.x2; x++)
                for (int y = this.y1; y <= this.y2; y++)
                    for (int z = this.z1; z <= this.z2; z++)
                        if (main.match().getCurrentWorld().getBlockAt(x, y, z).getType() == composure) // If this block matches the target composure..
                            blocks.add(main.match().getCurrentWorld().getBlockAt(x, y, z)); // Add this as part of the monument region.
            return blocks;
        }

        @EventHandler
        public void onBreak(BlockBreakEvent event) {
            if (event.getBlock().getType() == composure) { // Is it the material we're tracking?
                if (isInside(event.getBlock().getLocation())) { // Was the block a part of the monument?
                    WarPlayer wp = main.getWarPlayer(event.getPlayer()); // Get the WarPlayer implement.
                    if (wp.getCurrentTeam() == null) return; // Are they even playing?
                    if (wp.getCurrentTeam().getDisplayName().equals(owner)) { // Did they break their own monument?
                        event.setCancelled(true);
                        return;
                    }
                    region.remove(event.getBlock()); // They did, let's remove it from the monument blocks.
                    blocksBroken++; // Increment the amount of blocks remaining.

                    // Increment a player's footprint on the monument.
                    // Footprint being how much % they've destroyed.
                    if (!footprint.containsKey(wp.getPlayer().getUniqueId()))
                        footprint.put(wp.getPlayer().getUniqueId(), 1);
                    else footprint.put(wp.getPlayer().getUniqueId(), footprint.get(wp.getPlayer().getUniqueId()) + 1);

                    int calc = calculatePercentage(0); // Calculate the percentage of the monument remaining.

                    DTM dtm = (DTM) main.cache().getGamemode("Destroy The Monument"); // Get the DTM running instance.

                    if (calculatePercentage(1) == 100) { // Has this monument initially taken damage?
                        Bukkit.broadcastMessage(owner + "'s monument has been damaged!");
                        dtm.logEvent(wp.getTeamName() + " damaged " + owner + "'s monument");
                    }

                    if (calc <= 0) { // Has this monument been destroyed?
                        destroy();
                        Bukkit.broadcastMessage(owner + "'s monument has been destroyed!");
                        dtm.logEvent(wp.getTeamName() + " destroyed " + owner + "'s monument");
                    }

                    dtm.updateScoreboard();
                    if (dtm.checkWin()) // Has someone won?
                        dtm.onEnd(); // Good game!
                }
            }
        }

        @EventHandler
        public void onPlace(BlockPlaceEvent event) {
            // Don't allow blocks to be placed inside the monument region.
            if (isInside(event.getBlockPlaced().getLocation())) event.setCancelled(true);
        }

        @EventHandler
        public void onExplode(EntityExplodeEvent event) {
            ArrayList<Block> toRemove = new ArrayList<>();
            for (Block block : event.blockList())
                if (block.getType() == composure)
                    if (isInside(block.getLocation())) {
                        toRemove.add(block);
                    }

            // Don't allow explosions to damage the monument.
            event.blockList().removeAll(toRemove);
        }
    }
}
