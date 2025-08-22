package sbs.mira.pvp.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sbs.mira.pvp.framework.MiraPlayer;

/**
 * custom event to handle death during an active, ongoing pvp match.
 * <p>
 * Just like all events, custom ones can be created.
 * This one is called when a player dies during a match.
 * <p>
 * created on 2017-09-21.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.0
 * @see Event
 * @since 1.0.0
 */
public class MatchPlayerDeathEvent extends Event {
  
  private static final HandlerList handlers = new HandlerList();
  private final MiraPlayer dead, killer;
  
  public MatchPlayerDeathEvent(MiraPlayer dead, MiraPlayer killer) {
    this.dead = dead;
    this.killer = killer;
  }
  
  public static HandlerList getHandlerList() {
    return handlers;
  }
  
  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }
  
  /**
   * @return the victim.
   */
  public MiraPlayer getPlayer() {
    return dead;
  }
  
  /**
   * @return the killer.
   */
  public MiraPlayer getKiller() {
    return killer;
  }
}
