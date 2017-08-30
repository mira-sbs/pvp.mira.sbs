package au.edu.swin.war.game;

import au.edu.swin.war.framework.game.WarMap;
import au.edu.swin.war.framework.stored.Activatable;
import au.edu.swin.war.util.Match;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;

/**
 * An extension to WarMap.
 * <p>
 * This is the class that should be extended for map
 * configurations.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarMap
 * <p>
 * Created by Josh on 20/04/2017.
 * @since 1.0
 */
public abstract class Map extends WarMap {

    private Gamemode.Mode[] modes; // The defined gamemodes for this map.

    /**
     * Returns the gamemodes that are allowed to
     * be played on this map. For internal usage.
     *
     * @return Available gamemodes.
     */
    public Gamemode.Mode[] getGamemodes() {
        return modes;
    }

    /**
     * Defines the gamemodes that are allowed to
     * be played on this map.
     *
     * @see Gamemode.Mode
     */
    protected void setGamemodes(Gamemode.Mode[] modes) {
        this.modes = modes;
    }

    @Override
    public void activate() {
        // Register any map-specific events for the duration of the match.
        main.plugin().getServer().getPluginManager().registerEvents(this, main.plugin());
        // Activate the gamemode and begin the match!
        main.match().getCurrentMode().activate();

        setActive(true); // Sets the state of this map to active.
        postStart(); // Calls postStart(), see documentation.

        // Activates map objectives.
        for (Activatable obj : objectives())
            obj.activate();
    }

    @Override
    public void deactivate() {
        // Calls reset(), see documentation.
        reset();

        // De-activates the gamemode and begins the cycle.
        main.match().getCurrentMode().deactivate();

        // De-activates map objectives.
        for (Activatable obj : objectives())
            obj.deactivate();

        setActive(false); // Sets the state of this map to inactive.
        HandlerList.unregisterAll(this); // Unregister map-specific events.
    }

    /**
     * This method should spawn players around the spectator
     * spawn to avoid a giant clusterbomb of players.
     * <p>
     * For all intents and purposes, this is not needed yet.
     *
     * @return Spec spawn.
     * @since 1.0
     */
    public Location getSpectatorSpawn() {
        // Convert the serialized location to a Spigot location.
        return specSpawn.toLocation(main.match().getCurrentWorld(), true);
    }

    /**
     * This function is the same as above, except it
     * crafts the location from the previous match's
     * world.
     *
     * @return Spec spawn.
     * @since 1.0
     */
    public Location getSpectatorSpawn_() {
        // Convert the serialized location to a Spigot location.
        return specSpawn.toLocation(((Match) main.match()).getPreviousWorld(), true);
    }
}
