package sbs.mira.pvp.framework.game;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.event.MatchEndEvent;
import sbs.mira.pvp.framework.event.MatchPlayerJoinEvent;
import sbs.mira.pvp.framework.event.MatchPlayerLeaveEvent;
import sbs.mira.pvp.framework.stored.SerializedLocation;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.framework.util.WarMatch;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * This extensible class handles all gamemode-related
 * function that is commonly shared among classes.
 * When creating new gamemodes, make sure they extend
 * this class otherwise the program will NOT work.
 * <p>
 * Created by Josh on 20/03/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @since 1.0
 */
public abstract class WarMode implements Listener {

    private final Random rng; // A random number generator for any usage.
    protected boolean permaDeath; // Specifies that permanent death is enabled.
    protected boolean active; // Whether or not this class is active during a match.
    protected MiraPulse main; // The WarManager instance. This allows access to all other crucial modules.
    // !! IMPORTANT !! //
    /* Ensure that these fields are initialized & freed when needed. */
    private BukkitTask runtimeTask; // Global gamemode-specific runtime task.
    private Team spec; // Holds the Spigot team extension for the spectators.
    private int timeElapsed; // Specifies the number of seconds elapsed during the match.
    private Scoreboard score; // Holds the Spigot scoreboard extension that players see.
    private WarMap map; // The map currently associated with this gamemode.
    /* HashMaps that must be initialized/freed on a match start/end. */
    private HashMap<String, WarTeam> teams; // Temporary Key/Value set to hold maps for the match.
    private HashMap<String, ArrayList<SerializedLocation>> teamSpawns; // Temporary Key/Value set to hold Team spawns.

    /**
     * Since this class is intialized through reflections,
     * no parameters can be included in the constructor.
     * <p>
     * To work around this, init() is called after the
     * class has actually been initialized to set values.
     */
    public WarMode() {
        // Call init() externally
        rng = new Random();
    }

    /**
     * Calling this procedure is IMPORTANT. The program will
     * NOT work if you do not define the WarManager instance.
     * <p>
     * You only need to call this ONCE.
     *
     * @param main The WarManager instance.
     */
    public void init(MiraPulse main) {
        this.main = main;
        teams = new HashMap<>(); // The Key/Value set only needs to be cleared on a match end. Do not null or free it.
        teamSpawns = new HashMap<>(); // The same as above applies to the spawns. Please clear instead of nulling.
    }

    /**
     * Required procedure in all external classes.
     * Configure this procedure to reset everything that needs
     * to be fresh for when the gamemode is activated once
     * again. i.e. Team Death Match scores, etc.
     */
    public abstract void reset();

    /**
     * Possible required procedure in your Gamemode extension.
     * Configure this procedure to reset everything that needs
     * to be fresh for when the gamemode is activated once
     * again. i.e. Statistics.
     */
    public abstract void resetCommon();

    /**
     * ! IMPORTANT !
     * ! DO NOT CONFUSE THIS WITH INIT()!
     * <p>
     * This procedure should be configured in a way that all
     * appropriate values are loaded into the gamemode class
     * when the map is loaded also.
     * <p>
     * Do NOT load or change any fields that are a member of
     * this abstract class. Only initialize what is needed for
     * the specific gamemode, such as the TDM scores being set to 0.
     */
    public abstract void initialize();

    /**
     * This procedure is very similar to initialize().
     * This should be extended in your Gamemode class
     * to reset local, common variables in it.
     * <p>
     * i.e. Statistics counters
     */
    public abstract void initializeCommon();

    /**
     * This function simply makes a call to the gamemode
     * to update every player's scoreboard. This should be
     * done whenever a score changes, such as the amount of
     * points a team has on Team Death Match.
     * <p>
     * Try not to call this too much, as it is intensive and
     * can flicker a lot if not used carefully.
     * <p>
     * If the gamemode doesn't use a scoreboard, ignore this.
     *
     * @see org.bukkit.scoreboard.Scoreboard
     */
    public abstract void updateScoreboard();

    /**
     * This documentation will explain the 4 below functions:
     * <p>
     * 1. Returns the shortened abbreviation of a gamemode. i.e. TDM.
     * 2. Returns the full gamemode name. i.e. Team Death Match.
     * 3. Returns the offensive tactic. i.e. Kill enemy players!
     * 4. Returns the defensive tactic. i.e. Protect your teammates!
     *
     * @return The relevant result for the requested function.
     */
    public abstract String getName();

    public abstract String getFullName();

    public abstract String getOffensive();

    public abstract String getDefensive();

    /**
     * Returns the correct grammar of the WarMode for broadcasts.
     * For example, 'a' TDM, or 'an' FFA.
     * 1. The current match is a TDM at This Map!
     * 2. The current match is an FFA at This Map!
     * <p>
     * You wouldn't say an TDM or a FFA, would you?
     *
     * @return The correct grammar of the WarMode.
     */
    public abstract String getGrammar();

    /**
     * A procedure that is run when a player dies.
     * You must configure this yourself in an external gamemode class.
     * <p>
     * This procedure is automatically called when a player dies.
     * onDeath() is called when there is no killer repsonsible.
     *
     * @param killed The player who died.
     * @param killer The player's killer.
     * @see org.bukkit.event.entity.PlayerDeathEvent below.
     * <p>
     * An example of this procedure would be to credit the killer's team 1 point in TDM.
     */
    public abstract void onKill(MiraPlayer killed, MiraPlayer killer);

    /**
     * A procedure that is run when a player dies.
     * This is different to onKill, because no player
     * was responsible for the player's death.
     * You must configure this yourself in an external gamemode class.
     * <p>
     * This procedure is automatically called when a player dies.
     *
     * @param killed The player who died.
     * @see org.bukkit.event.entity.PlayerDeathEvent below.
     * <p>
     * An example of this procedure would be to credit the killer's team 1 point in TDM.
     */
    public abstract void onDeath(MiraPlayer killed);

    /**
     * Called when a player leaves.
     * You must configure this yourself in an external gamemode class.
     * As stated above, this is called automatically.
     * <p>
     * An example of this procedure would be to penalize a team or modify
     * the match in some way to compensate for the player leaving.
     * i.e. a flagholder in CTF leaving the match.
     *
     * @param left The player who left.
     */
    public abstract void onLeave(MiraPlayer left);

    /**
     * This performs the opposite of above, so read the documentation
     * that is provided above. Please extend and utilise.
     * <p>
     * An example of this procedure would be to modify the match in
     * some way to compensate for the player joining.
     * <p>
     * i.e. to display appropriate data to the
     * player depending on what team they joined.
     *
     * @param joined The player who joined.
     */
    public abstract void onJoin(MiraPlayer joined);

    /**
     * A function that is run when the match is ended.
     * This is an essential function in gamemode management.
     * It must be called when the objective is fulfilled or
     * if an operator decides to end the match with a command.
     * <p>
     * This calls decideWinner(), so any gamemode-specific
     * conclusion logic should be done there, such as broadcasting
     * the winning team or player and the results.
     */
    public void onEnd() {
        if (runtimeTask != null) {
            runtimeTask.cancel();
            decideWinner();
        }
        finish();
    }

    /**
     * A function that was briefly explained in onEnd().
     * Any gamemode-specific conclusion logic should be
     * done here, such as broadcasting the winning team
     * or player and the results.
     * <p>
     * i.e. in Team Death Match
     * -> Broadcast the team(s) with the most points
     * -> Broadcast the final results of each team
     */
    protected abstract void decideWinner();

    /**
     * Returns the gamemode's loaded teams, if any.
     * Teams should only be loaded in this gamemode
     * if there is a match running and this was the
     * gamemode that was selected.
     *
     * @return The active teams Key/Value set.
     */
    public Collection<WarTeam> getTeams() {
        return teams.values();
    }

    /**
     * This procedure should be called from the external class
     * once a match has fulfilled its criteria.
     * <p>
     * i.e. time running out in TDM,
     * i.e. reaching score cap in FFA, etc.
     */
    private void finish() {
        main.plugin().getServer().getPluginManager().callEvent(new MatchEndEvent()); // Call a custom event.
        setActive(false); // Sets the gamemode instance to inactive.
        if (teamSpawns != null)
            teamSpawns.clear(); // CLEAR the Key/Value set, do not free it.
        main.match().matchEnd();
    }

    /**
     * Increases the time elapsed in the match by 1.
     * This procedure is automatically called every
     * second by the runtimeTask.
     *
     * @see org.bukkit.scheduler.BukkitRunnable;
     */
    private void incrementTimeElapsed() {
        timeElapsed += 1;
    }

    /**
     * Returns the amount of time elapsed during this match.
     *
     * @return The amount of time elapsed.
     */
    public int getTimeElapsed() {
        return timeElapsed;
    }

    /**
     * Sets -specifically- the amount of time elapsed
     * in the match. Mainly for debugging purposes.
     */
    public void setTimeElapsed(int timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    /**
     * Returns whether or not the current gamemode has permanent
     * death enabled. This should be used when respawning to not
     * notify the player that they will respawn as it is permanent
     * death.
     *
     * @return Perma death?
     */
    public boolean isPermaDeath() {
        return permaDeath;
    }

    /**
     * Returns the current map associated with this gamemode
     * during the current match. This is stored temporarily
     * so certain attributes in the WarMap class can be accessed
     * during runtime.
     *
     * @return The current associated map.
     */
    protected WarMap map() {
        return map;
    }

    /**
     * Awaken this gamemode for the match. A -LOT- of things
     * will be done automatically here, and will be documented.
     * <p>
     * In a nutshell, once everything is good to go and the match
     * has started, this will awaken the gamemode and objectives
     * will become available.
     *
     * @see java.lang.Runnable
     * @see org.bukkit.scheduler.BukkitScheduler
     */
    @SuppressWarnings("unchecked")
    public void activate() {
        main.plugin().getServer().getPluginManager().registerEvents(this, main.plugin()); // Allows the server to listen in on events for this gamemode class.
        map = main.cache().getCurrentMap();

        for (WarTeam team : map().getTeams())
            // Copies every WarTeam defined in the map over to the gamemode!
            teams.put(team.getTeamName(), team.clone());

        // Copies every spawnpoint for every team defined in the map also!
        teamSpawns = (HashMap<String, ArrayList<SerializedLocation>>) map().teamSpawns.clone();

        setActive(true); // Sets this gamemode as active and will be recognised as so by the program.
        score = main.plugin().getServer().getScoreboardManager().getNewScoreboard(); // Create a new scoreboard. (Spigot)

        for (WarTeam team : teams.values()) {
            Team lTeam = score.registerNewTeam(team.getTeamName()); // Creates a Spigot Team instance for this team.
            team.setBukkitTeam(lTeam); // Assigns the Spigot Team to the copied WarTeam instance.
            lTeam.setCanSeeFriendlyInvisibles(true); // Allows teammates to see each other when visible. (Spigot)
            lTeam.setAllowFriendlyFire(false); // Disables friendly fire for teammates. (Spigot)
            lTeam.setPrefix(team.getTeamColor() + ""); // Sets the player's name color to the team's color. (Spigot)
        }

        spec = score.registerNewTeam("Spectators"); // Manually defines the spectator team. (Spigot)
        spec.setCanSeeFriendlyInvisibles(true); // Allows spectators to see each other. (Spigot)
        spec.setAllowFriendlyFire(false); // Disables friendly fire. (Spigot)
        spec.setPrefix(ChatColor.LIGHT_PURPLE + ""); // Spectators are purple!!! (Spigot)

        for (MiraPlayer wp : main.getWarPlayers().values())
            spec.addPlayer(wp.crafter()); // Adds every player to the spectator team by default. (Spigot)

        initializeCommon(); // Initializes common values in the extended gamemode class.
        initialize(); // Initializes everything in the external gamemode class!

        // Defines the plugin executing the timer and the runnable interface. (Spigot)
        runtimeTask = Bukkit.getScheduler().runTaskTimer( // Runs a task timer at a regular interval. (Spigot)
                main.plugin(), () -> {
                    if (main.match().getStatus() != WarMatch.Status.PLAYING) {
                        // Cancel this if the match is not currently active.
                        runtimeTask.cancel();
                        return;
                    }
                    incrementTimeElapsed(); // Increments the time elapsed, every second!

                    long timeLeft = getMatchDuration() - getTimeElapsed(); // Calculates the amount of time remaining.
                    if (timeLeft % 60 == 0 && timeLeft != 0) { // Checks that the time is a remainder of
                        long minutes = (timeLeft / 60); // Calculates number of minutes remaining.
                        String s = (minutes == 1 ? "" : "s"); // Should it be 'minute' or 'minutes'?

                        // Broadcasts the amount of minutes remaining.
                        Bukkit.broadcastMessage("There is " + minutes + " minute" + s + " remaining!");
                    } else if (timeLeft == 30) {
                        // Broadcasts that there is 30 seconds remaining.
                        Bukkit.broadcastMessage("There is " + timeLeft + " seconds remaining!");
                    } else if (timeLeft < 6 && timeLeft > 0) {
                        String s = (timeLeft == 1 ? "" : "s"); // Calculates number of seconds remaining.

                        // Broadcasts the amount of seconds.
                        Bukkit.broadcastMessage("There is " + timeLeft + " second" + s + " remaining!");
                    }

                    tick(); // Allows the external class to execute certain procedures every second too.

                    if (getTimeElapsed() >= getMatchDuration())
                        onEnd(); // If the time is up, end the match even if the objective is not complete.
                }, 0L, 20L); // Have a 0 tick delay before starting the task, and repeat every 20 ticks.
        // ! IMPORTANT ! A 'tick' is a 20th of a second. Minecraft servers run at 20 ticks per second. (TPS)
    }

    /**
     * Automatically balances everyone onto teams.
     */
    protected void autoAssign() {
        // Keep a temporary list of people who have not being assigned to a team.
        ArrayList<MiraPlayer> targets = new ArrayList<>(main.getWarPlayers().values());
        while (targets.size() != 0) { // Keep looping until this array is empty.
            MiraPlayer target = targets.get(rng.nextInt(targets.size())); // Gets a random player.
            if (target.isJoined()) {
                // If joined, use entryHandle() to put them on the lowest team.
                entryHandle(target);
                if (!target.isJoined()) {
                    // If, for some reason, they did not get put on a team, assume them as spectating.
                    target.crafter().setGameMode(GameMode.CREATIVE);
                    main.giveSpectatorKit(target);
                }
            } else {
                // They don't want to play. Assume them as spectating.
                target.crafter().setGameMode(GameMode.CREATIVE);
                main.giveSpectatorKit(target);
            }
            targets.remove(target);
        }
    }

    /**
     * This procedure is automatically called by the runtimeTask
     * every 20 ticks, or every 1 second. You must configure this
     * procedure, but you don't have to use it if it isn't needed.
     * <p>
     * An example usage of this would be to shoot up a firework every
     * 20 ticks at a flagholder's location to show everyone else where
     * they currently are.
     */
    public abstract void tick();

    /**
     * This procedure incapacitates the gamemode after the match
     * has been completed. Listeners are disabled, and all fields
     * that were changed during the match are reset.
     */
    public void deactivate() {
        if (runtimeTask != null) runtimeTask.cancel(); // If the task isn't null already, cancel the task first.
        runtimeTask = null; // Free up the task in memory.
        HandlerList.unregisterAll(this); // Unregister all listener handlers for this class. (Spigot)
        setActive(false); // Sets this gamemode as inactive and will be ignored by the program.
        resetCommon(); // Resets common values in external Gamemode class.
        reset(); // Resets any other values in the external class.
        resetLocalValues(); // Resets values defined in this class as stated below.
        map = null; // Frees up the currently playing map's assignment in memory.
    }

    /**
     * Similar to reset(), this procedure will automatically
     * reset any values that are commonly shared amongst all
     * gamemode classes, such as the time elapsed.
     */
    private void resetLocalValues() {
        timeElapsed = 0; // Sets time elapsed back to 0 seconds.
        permaDeath = false; // Sets permadeath for this gamemode back to the default of false.

        for (WarTeam team : teams.values()) {
            for (OfflinePlayer pl : team.getBukkitTeam().getPlayers())
                team.getBukkitTeam().removePlayer(pl); // Removes the player from the defined Spigot team. (Spigot)
            team.setBukkitTeam(null); // Sets the associated Spigot team to null to free up memory.
        }
        spec = null; // Removes the spectator team to free up memory.
        score = Bukkit.getScoreboardManager().getNewScoreboard(); // Re-assign the scoreboard field with a fresh one.
        if (teams != null)
            teams.clear(); // Clear associated teams to free up memory.
    }

    /**
     * Quick function to return the global Scoreboard.
     * This is to make the code look cleaner.
     *
     * @return The scoreboard associated with this gamemode.
     */
    public Scoreboard s() {
        return score;
    }

    /**
     * Returns whether or not this gamemode is marked
     * as active or not. A gamemode should only be marked
     * as active when the match playing is the gamemode
     * in question.
     *
     * @return Whether the gamemode is active or not.
     */
    private boolean isActive() {
        return active;
    }

    /**
     * Sets the gamemode as 'active' or not.
     * As stated above, only the associated gamemode
     * should be marked as active during a match.
     *
     * @param active Whether the core is active or not.
     */
    private void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Handles entry into and out of the match.
     * If the player tries to enter a match during permadeath, deny it.
     * If the player joins, assign their team and call onJoin().
     * If the player leaves, disassociate them and call onLeave();
     * If the match is permadeath, do not message the player.
     *
     * @param wp         The player to handle.
     * @param preference What team the player would like to be on. (if supplied)
     */
    public void entryHandle(MiraPlayer wp, WarTeam... preference) {
        Player pl = wp.crafter(); // Returns Spigot's implementation of Player. (Spigot)
        if (!isActive()) return; // If this gamemode is not active, do not execute anything.
        if (permaDeath && wp.isJoined()) {
            // Alert the player that permanent death is enabled and cancel the entry.
            pl.sendMessage("You are too late to join!");
            wp.setJoined(false);
        } else if (wp.isJoined()) {
            // Assign the player to their team and call onJoin() for the external class.
            if (preference != null && preference.length == 1)
                carryOutTeam(wp, preference[0]);
            else
                carryOutTeam(wp, getSmallestTeam());
            main.plugin().getServer().getPluginManager().callEvent(new MatchPlayerJoinEvent(wp)); // Call an event.
        } else { // If the player did not join, execute a leaving handle.
            if (!permaDeath)
                pl.sendMessage("You have left the match!"); // Alert the player physically if this is not a permadeath match.
            WarTeam team = wp.getCurrentTeam(); // Returns the player's associated team for temporary use.
            wp.setCurrentTeam(null); // Disassociates the player with their team.
            pl.teleport(map().getSpectatorSpawn()); // Teleports the player to the map's spectator spawnpoint. (Spigot)
            pl.setGameMode(GameMode.CREATIVE); // Sets the player to spectator mode. (Spigot)
            team.getBukkitTeam().removeEntry(pl.getName()); // Removes the player from their Spigot team. (Spigot)
            spec.addEntry(pl.getName()); // Assigns the player to the spectator team. (Spigot).
            main.items().clear(wp); // Clears the player's inventory.
            main.giveSpectatorKit(wp); // Gives the player a spectator kit.
            onLeave(wp); // Calls onLeave() for the external class.
            main.plugin().getServer().getPluginManager().callEvent(new MatchPlayerLeaveEvent(wp)); // Call an event.
        }
    }

    /**
     * If the player is joining the match, this procedure
     * acts to carry out the player to an assigned team.
     * <p>
     * This procedure assigns the player to a team.
     * <p>
     * //TODO: Add team preference for debug?
     *
     * @param dp   The player to assign a team.
     * @param team The team to assign to a player.
     */
    private void carryOutTeam(MiraPlayer dp, WarTeam team) {
        Player pl = dp.crafter(); // Assigns Spigot player implementation.

        if (team.isFull()) {
            //
            pl.sendMessage("All teams are full, please try joining later.");
            dp.setJoined(false);
            return;
        }

        pl.teleport(randomSpawnFrom(teamSpawns.get(team.getTeamName())).toLocation(main.match().getCurrentWorld(), true)); // Teleports player to random team spawnpoint. (Spigot)
        pl.setGameMode(GameMode.SURVIVAL); // Sets the player's gamemode to survival. (Spigot)
        pl.setFallDistance(0F); // Reset fall distance. (Spigot)
        dp.setCurrentTeam(team); // Assigns the player's team.
        spec.removeEntry(pl.getName()); // Removes the player from the spectator team. (Spigot)
        team.getBukkitTeam().addEntry(pl.getName()); // Assigns the player to the team's Spigot team. (Spigot)
        map().applyInv(dp); // Applies the map's inventory to the player.

        TextComponent comp = new TextComponent("You have joined the ");
        comp.addExtra(team.getHoverInformation());
        pl.spigot().sendMessage(comp);

        onJoin(dp);
    }

    /**
     * Searches through all current teams in the match for
     * the team with the least amount of members.
     *
     * @return The team with the least members.
     */
    private WarTeam getSmallestTeam() {
        WarTeam found = null; // The 'result' field.
        int size = -1; // The initial 'highest' size.
        for (WarTeam team : teams.values()) { // Loops through every team.
            if (size == -1) {
                found = team; // Recognises this team as the one with the least amount of members.
                size = team.getBukkitTeam().getEntries().size(); // Assigns the lowest amount of members to the amount of members in this team.
            } else if (team.getBukkitTeam().getEntries().size() < size) {
                found = team; // Sets this as the smallest team.
                size = team.getBukkitTeam().getEntries().size(); // Assigns new smallest team size.
            }
        }
        return found;
    }

    /**
     * Returns how long the map is configured to run for.
     * All maps must end after a certain period of time.
     *
     * @return The duration of the map.
     */
    private Long getMatchDuration() {
        return (Long) map().attributes.get("matchDuration");
    }

    /**
     * Randomly picks a value from a list.
     * This is just to randomly return a team spawn.
     *
     * @param array The list.
     * @return The value.
     */
    protected SerializedLocation randomSpawnFrom(List<SerializedLocation> array) {
        Random picker = new Random();
        return array.get(picker.nextInt(array.size()));
    }
}
