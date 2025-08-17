package sbs.mira.pvp.framework;

import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * oh look another mira stan.
 * created on 2017-03-21.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract class MiraPlayer<M extends MiraPulse> {
  
  protected final M mira;
  protected final CraftPlayer player;
  
  /**
   * @param player is for me?
   * @param mira   anchorrr.
   */
  public MiraPlayer(CraftPlayer player, M mira) {
    this.mira = mira;
    this.player = player;
  }
  
  /**
   * @see org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer
   */
  public CraftPlayer crafter() {
    return player;
  }
  
  /**
   * @param message The message to send to the player.
   * @see Player#sendMessage(String)
   */
  public void dm(String message) {
    player.sendMessage(message);
  }
  
  /**
   * @return the current in game name of this mira stan.
   * @see Player#getName()
   */
  public String name() {
    return player.getName();
  }
  
  /**
   * @return formatted "display" name with formatting enabled+encouraged.
   * @see org.bukkit.entity.Player#getName()
   */
  public String display_name() {
    return player.getDisplayName();
  }
}
