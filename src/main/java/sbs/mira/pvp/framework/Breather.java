package sbs.mira.pvp.framework;

import org.jetbrains.annotations.NotNull;

/**
 * 🫀
 *
 * @author jj.mira.sbs
 * @version 1.0.0
 * @since 1.0.0
 */
public
interface Breather<Pulse extends MiraPulse<?, ?>>
{
  
  /**
   * @return heartbeat of mira, still going i hope.
   * @throws FlatlineException bruh...
   */
  @NotNull
  Pulse pulse() throws FlatlineException;
  
  void breathe(@NotNull Pulse pulse) throws IllegalStateException;
  
  /**
   * just set the pulse brah?
   */
  class FlatlineException
    extends RuntimeException
  {
  }
}
