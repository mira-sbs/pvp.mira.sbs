package sbs.mira.pvp.framework.event;

import org.jetbrains.annotations.NotNull;
import sbs.mira.pvp.framework.MiraPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * custom event to handle team joining.
 * created on 2017-09-21.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see Event
 * @since 1.0.0
 */
public class MatchPlayerJoinEvent extends Event {
  
  private static final HandlerList handlers = new HandlerList();
  private final MiraPlayer player;
  
  public MatchPlayerJoinEvent(MiraPlayer player) {
    this.player = player;
  }
  
  public static HandlerList getHandlerList() {
    return handlers;
  }
  
  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }
  
  public MiraPlayer getPlayer() {
    return player;
  }
}
