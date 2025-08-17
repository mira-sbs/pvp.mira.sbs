package sbs.mira.pvp.framework;

import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * oh look another mira stan.
 * created on 2017-03-21.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract
class MiraPlayer<Pulse extends MiraPulse<?, ?>>
  implements Breather<Pulse>
{
  
  private @Nullable Pulse pulse;
  protected final @NotNull CraftPlayer player;
  
  /**
   * @param player is for me?
   * @param pulse  anchorrr.
   */
  public
  MiraPlayer(@NotNull CraftPlayer player, @NotNull Pulse pulse)
  {
    this.pulse = pulse;
    this.player = player;
  }
  
  @Override
  public @NotNull
  Pulse pulse() throws FlatlineException
  {
    if (this.pulse != null)
    {
      return pulse;
    }
    else
    {
      throw new FlatlineException();
    }
  }
  
  @Override
  public
  void breathe(@NotNull Pulse pulse) throws IllegalStateException
  {
    if (this.pulse == null)
    {
      this.pulse = pulse;
    }
    else
    {
      throw new IllegalStateException("a breather may not have two pulses.");
    }
  }
  
  /**
   * @see org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer
   */
  public @NotNull
  CraftPlayer crafter()
  {
    return player;
  }
  
  /**
   * @param message The message to send to the player.
   * @see Player#sendMessage(String)
   */
  public
  void dm(String message)
  {
    player.sendMessage(message);
  }
  
  /**
   * @return the current in game name of this mira stan.
   * @see Player#getName()
   */
  public
  String name()
  {
    return player.getName();
  }
  
  /**
   * @return formatted "display" name with formatting enabled+encouraged.
   * @see org.bukkit.entity.Player#getName()
   */
  public
  String display_name()
  {
    return player.getDisplayName();
  }
}
