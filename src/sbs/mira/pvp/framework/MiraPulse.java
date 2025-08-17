package sbs.mira.pvp.framework;

import org.jetbrains.annotations.NotNull;

/**
 * [recursive wit.]
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see MiraPlugin
 * @since 1.0.0
 */
public abstract
class MiraPulse
{
  @NotNull
  public abstract
  MiraPlugin plugin();
  
  @NotNull
  MiraPluginMaster master();
}
