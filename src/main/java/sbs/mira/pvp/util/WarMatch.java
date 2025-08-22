package sbs.mira.pvp.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import sbs.mira.pvp.framework.MiraModule;
import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.framework.game.WarMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This (extendable) class controls all flow of match cycles.
 * While not directly interfering with rounds, its functions
 * are defined here for a ground point to manifest the movement
 * of the match to the next stage of cycling.
 * <p>
 * i.e. Starting match -> Match started
 * i.e. Match ending -> Cycle
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see Bukkit
 * @since 1.0
 */
public abstract class WarMatch extends MiraModule {

    public int rotationPoint; // Holds the point at which the rotation is at.
    private WarMode currentMode; // Holds the current gamemode's running instance.
    private Status status; // Holds the current state of the match.
    private String cyclePrevMap; // Holds the map played prior to the last cycle.
    private String currentMap; // Holds the current map being played, if any.
    private long roundID; // A unique, random 5-digit number for the Minecraft world name.
    private List<String> rotationList; // Holds a list of map names that are on the rotation.

    /**
     * Constructor of the War Match manager.
     * Calling this will setup your default variables + the rotation.
     */
    public WarMatch(MiraPulse main) {
        super(main);

        //Set up match variables.
        status = Status.NONE;
        cyclePrevMap = "None";
        currentMap = "None";
        currentMap = null;

        //Set up rotation list.
        try (Stream<String> stream = Files.lines(Paths.get(mira().plugin().getDataFolder() + File.separator + "rotation"))) {
            rotationList = new ArrayList<>();
            stream.forEachOrdered(rotationList::add); // For each line read, add it to the list.
        } catch (IOException e) {
            e.printStackTrace();
            mira().plugin().getServer().shutdown(); // Don't play without a rotation.
        }
    }

    /**
     * Returns the 5-digit ID of the map's Minecraft world.
     * This is randomly generated so files do not duplicate
     * or clash when a round cycles.
     *
     * @return Round ID.
     */
    private String getRoundID() {
        return roundID + "";
    }

    /**
     * Redefines the round ID being used by the match.
     *
     * @param roundID New round ID.
     */
    protected void setRoundID(long roundID) {
        this.roundID = roundID;
    }

    /**
     * Returns the 5-digit ID of the map's Minecraft world.
     * Some procedures require the number as a number and
     * not as a string, so this is the method that returns it.
     *
     * @return Round it.
     */
    public long getRawRoundID() {
        return roundID;
    }

    /**
     * Checks whether or not the player can interact in a match
     * based on their state and whether or not they're an admin.
     *
     * @param pl          The player, to check their state.
     * @param adminBypass If true, returns true always if player is admin.
     * @return Whether or not the player can interact.
     */
    public boolean canInteract(Entity pl, boolean adminBypass) {
        MiraPlayer wp = mira().getWarPlayer(pl.getUniqueId());
        return wp == null || (wp.is_member_of_team() || adminBypass && mira().plugin().has_permission(pl, "war.admin"));
    }

    /**
     * Checks whether this played should be affected by something
     * in a match. Basically returns false if not a spectator.
     *
     * @param pl The player, to check their state.
     * @return Whether or not they are affected.
     */
    public boolean isAffected(Entity pl) {
        MiraPlayer wp = mira().getWarPlayer(pl.getUniqueId());
        return wp != null && wp.is_member_of_team();
    }

    /**
     * Translates the #getRoundID() result into a Bukkit
     * world that can be manipulated further.
     *
     * @return The world associated with the round ID.
     */
    public World getCurrentWorld() {
        if (status == Status.NONE) {
            mira().plugin().log("A null world may have been returned!");
            return null;
        } else return Bukkit.getWorld(getRoundID());
    }

    /**
     * Returns the current map playing, by its name.
     * If you want the running instance, you should
     * probably use WarCache's getCurrentMap().
     *
     * @return The current map's name.
     */
    protected String getCurrentMap() {
        return currentMap;
    }

    /**
     * Sets the current map playing, by name.
     * This does not set the running instance,
     * but the function that does uses this variable.
     *
     * @param currentMap The current map's name.
     */
    protected void setCurrentMap(String currentMap) {
        this.currentMap = currentMap;
    }

    /**
     * Returns the map that was previously played.
     * This is used when the previous map needs to
     * be known.
     * <p>
     * When a player connects, the previous map needs
     * to be known. If the match is cycling or under
     * a vote, players are still in the previous world
     * even though the new world has been assigned.
     *
     * @return The previous map played.
     */
    public String getPreviousMap() {
        return cyclePrevMap;
    }

    /**
     * Sets the previous map played, by name.
     * This should be used to set the previous
     * map for checks when needed.
     *
     * @param cyclePrevMap The previous map's name.
     */
    protected void setPreviousMap(String cyclePrevMap) {
        this.cyclePrevMap = cyclePrevMap;
    }

    /**
     * Returns the currently active gamemode.
     * This returns a running instance of the mode.
     *
     * @return The gamemode.
     */
    public WarMode getCurrentMode() {
        return currentMode;
    }

    /**
     * Sets the current gamemode playing by running instance.
     * Once a vote has been completed, it should be set using this.
     *
     * @param currentMode The gamemode.
     */
    protected void setCurrentMode(WarMode currentMode) {
        this.currentMode = currentMode;
    }

    /**
     * Returns the current state of the match.
     *
     * @return Match state.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Redefines the current state of the match.
     *
     * @param status The new match state.
     */
    protected void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Returns the currently loaded rotation list.
     * Used to display to a player who requests it.
     *
     * @return The rotation list.
     */
    public List<String> getRotationList() {
        return rotationList;
    }

    /**
     * This is called once a cycle is over and a
     * new map is ready to be played on.
     * <p>
     * This should:
     * Transfer current map to the previous map.
     * Determine the next map and allocate it.
     * Hold a vote for a multi-gamemode map if applicable.
     * Recycle and clear variables used in voting logic.
     * Then call preMatch().
     */
    public abstract void endCycle();

    /**
     * This is called once a new map has been
     * allocated and it must be prepped for usage.
     * <p>
     * This should:
     * Copy the physical map data over for temporary usage.
     * Assign a random 5-digit temporary ID for the world.
     * Load the map and teleport all players into it.
     * Unload the previous map and discard any unneeded data.
     * Start a countdown, then call matchStart();
     */
    public abstract void preMatch();

    /**
     * Once a match starts, the map will take over
     * the flow of the match, assigning players to
     * teams, initialising the gamemode, etc.
     * <p>
     * There is no need to call matchEnd() in this procedure.
     */
    public abstract void matchStart();

    /**
     * Called when a match is ended. This procedure
     * should not be called intrinsically to end a
     * match, but after the map has faciliated the
     * end to one.
     * <p>
     * This will make everyone a spectator and clean everything.
     * Afterwards, startCycle() should be called to keep the flow.
     */
    public abstract void matchEnd();

    /**
     * This allows a brief amount of time to pass for reflection.
     * Every now and then, fireworks or special effects should
     * appear out of the winning team or player to congratulate.
     * <p>
     * Call endCycle() after we're all done here.
     */
    public abstract void startCycle();

    /**
     * This enumerated type ensures accuracy & readability when
     * defining the the status of a round.
     * <p>
     * The NONE status is used when no match has been established.
     */
    public enum Status {
        VOTING,
        STARTING,
        PLAYING,
        CYCLE,
        NONE
    }
}
