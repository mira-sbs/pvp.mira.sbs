package au.edu.swin.war.event;

import au.edu.swin.war.framework.WarPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event to handle respawns.
 * <p>
 * Just like all events, custom ones can be created.
 * This one is called when a player has waited out
 * the full length of the respawn utility task.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see Event
 * <p>
 * Created by Josh on 20/04/2017.
 * @since 1.0
 */
public class MatchPlayerRespawnEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final WarPlayer player;

    public MatchPlayerRespawnEvent(WarPlayer player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Returns the player who respawned.
     *
     * @return The player who respawned.
     */
    public WarPlayer getPlayer() {
        return player;
    }
}
