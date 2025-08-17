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
public
class MiraPulse<Plugin extends MiraPlugin<MiraPulse<?, ?>>, Master extends MiraPluginMaster<?, ?>>
{
  @NotNull
  private final Plugin plugin;
  @NotNull
  private final Master master;
  
  public
  MiraPulse(@NotNull Plugin plugin, @NotNull Master master)
  {
    this.plugin = plugin;
    this.master = master;
  }
  
  @NotNull
  public
  Plugin plugin()
  {
    return plugin;
  }
  
  @NotNull
  public
  Master master()
  {
    return master;
  }
}
