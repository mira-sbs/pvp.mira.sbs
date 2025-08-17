package sbs.mira.pvp.event;

import sbs.mira.pvp.framework.MiraPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import sbs.mira.pvp.game.util.SpawnArea;

/**
 * Custom event to handle events after a match respawn.
 * Known uses: SpawnArea
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see SpawnArea
 * <p>
 * Created by Josh on 20/04/2017.
 * @since 1.0
 */
public class PostMatchPlayerRespawnEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final MiraPlayer player;

    public PostMatchPlayerRespawnEvent(MiraPlayer player) {
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
    public MiraPlayer getPlayer() {
        return player;
    }
}
