package sbs.mira.pvp;

import org.jetbrains.annotations.NotNull;
import sbs.mira.pvp.framework.MiraPlugin;
import sbs.mira.pvp.util.modules.CommandUtility;
import sbs.mira.pvp.util.modules.StatsCommandUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * [witty comment here.]
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @version 1.0.1
 * @see sbs.mira.pvp.framework.MiraPlugin
 * @since 1.0.0
 */
public class MiraPvpPlugin extends MiraPlugin<MiraPvp> {
  
  /**
   * required method by WarPlugin.
   * acts as the program's "Main()".
   */
  public void onEnable() {
    log("War program has awoken!");
    this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    
    final MiraPvpPlugin plugin = this;
    final MiraPvpMaster master = new MiraPvpMaster(this);
    
    mira = new MiraPvp() {
      @Override
      public @NotNull MiraPvpPlugin plugin() {
        return plugin;
      }
      
      @Override
      public @NotNull MiraPvpMaster master() {
        return master;
      }
    };
    
    register_module(CommandUtility.class);
    register_module(StatsCommandUtility.class);
    register_module_commands();
    
    mira.master().match().firstMatch(); // Start the special first round procedure to kick off the cycle.
  }
  
  /**
   * requires method by WarPlugin.
   * called when this program is shut down.
   */
  public void onDisable() {
    for (Player online : Bukkit.getOnlinePlayers())
      online.kickPlayer(getServer().getShutdownMessage());
    master().world().restoreMap(master().match().getRawRoundID() + ""); // Delete the current match world on shutdown.
  }
}
