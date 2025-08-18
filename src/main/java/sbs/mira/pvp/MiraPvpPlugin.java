package sbs.mira.pvp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import sbs.mira.core.MiraPlugin;
import sbs.mira.pvp.util.modules.CommandUtility;
import sbs.mira.pvp.util.modules.StatsCommandUtility;

/**
 * [witty comment here.]
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @version 1.0.1
 * @see sbs.mira.core.MiraPlugin
 * @since 1.0.0
 */
public
class MiraPvpPlugin
  extends MiraPlugin<MiraPvpPulse>
{
  /**
   * required method by WarPlugin.
   * acts as the program's "Main()".
   */
  @Override
  public
  void onEnable()
  {
    log("War program has awoken!");
    this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    
    final MiraPvpMaster master = new MiraPvpMaster(this);
    
    super.breathe(new MiraPvpPulse(this, master));
    master.breathe(pulse());
    
    register_module(CommandUtility.class);
    register_module(StatsCommandUtility.class);
    register_module_commands();
    
    pulse().master().match().firstMatch(); // Start the special first round procedure to kick off the cycle.
  }
  
  /**
   * requires method by WarPlugin.
   * called when this program is shut down.
   */
  public
  void onDisable()
  {
    for (Player online : Bukkit.getOnlinePlayers())
    {
      online.kickPlayer(getServer().getShutdownMessage());
    }
    pulse().master().world().restoreMap(pulse()
                                          .master()
                                          .match()
                                          .getRawRoundID() + ""); // Delete the current match world on shutdown.
  }
}
