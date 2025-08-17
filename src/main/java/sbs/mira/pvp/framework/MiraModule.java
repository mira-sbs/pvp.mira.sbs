package sbs.mira.pvp.framework;

import org.bukkit.event.Listener;

/**
 * a moving cog within the mira framework.
 * slide it into place (as needed) to turn neighbouring gears.
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @version 1.0.1
 * @since 1.0.0
 */
public abstract class MiraModule implements Listener {
  
  private final MiraPulse mira;
  
  protected MiraModule(MiraPulse main) {
    this.mira = main;
  }
  
  /**
   * don't get lost.
   *
   * @return stay with us.
   */
  public
  MiraPulse mira() {
    return mira;
  }
}
