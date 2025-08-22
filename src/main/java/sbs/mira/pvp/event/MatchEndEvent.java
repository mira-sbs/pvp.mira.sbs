package sbs.mira.pvp.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * [wit.]
 * created on 2017-09-21.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.0
 * @see Event
 * @since 1.0.0
 */
public class MatchEndEvent extends Event {
  
  private static final HandlerList handlers = new HandlerList();
  
  public static HandlerList getHandlerList() {
    return handlers;
  }
  
  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }
}
