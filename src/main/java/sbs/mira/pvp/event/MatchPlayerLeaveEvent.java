package sbs.mira.pvp.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import sbs.mira.pvp.framework.MiraPlayer;

/**
 * Custom event to handle team leaving.
 * <p>
 * Just like all events, custom ones can be created.
 * This one is called when a player leaves the match.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see Event
 * <p>
 * Created by Josh on 21/09/2017.
 * @since 1.1
 */
public class MatchPlayerLeaveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final MiraPlayer player;

    public MatchPlayerLeaveEvent(MiraPlayer player) {
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
     * Returns the player who leaves.
     *
     * @return The player who leaves.
     */
    public MiraPlayer getPlayer() {
        return player;
    }
}
