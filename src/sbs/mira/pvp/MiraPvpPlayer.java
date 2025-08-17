package sbs.mira.pvp;

import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.jetbrains.annotations.Nullable;
import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.stats.WarStats;
import org.bukkit.ChatColor;

/**
 * An extension to WarPlayer that contains non-framework fields.
 * Mostly used for statistics tracking.
 * created on 2025-08-17.
 *
 * @author jj.mira.sbs, jd.mira.sbs
 * @version 1.0.1
 * @see org.bukkit.plugin.java.JavaPlugin
 * @since 1.0.0
 */
public final class MiraPvpPlayer extends MiraPlayer<MiraPvp> {
  
  /**
   * @see sbs.mira.pvp.stats.WarStats
   */
  private final WarStats stats;
  
  /***
   * true if the player has indicated that they would like to join a team (once available).
   */
  private boolean joined;
  /**
   * @see sbs.mira.pvp.framework.game.WarTeam
   */
  private WarTeam team;
  
  public MiraPvpPlayer(WarStats stats, CraftPlayer player, MiraPvp mira) {
    super(player, mira);
    
    this.stats = stats;
    this.joined = false;
    this.team = null;
    
    changes_visibility();
  }
  
  /**
   * @see #stats
   */
  public WarStats stats() {
    return stats;
  }
  
  /**
   * @see #joined
   */
  public boolean joined() {
    return joined;
  }
  
  /**
   * @see #joined
   */
  public void joined(boolean joined) {
    this.joined = joined;
  }
  
  /**
   * @return true if the mira pvp stan has an [sic, lol] designated team.
   */
  public boolean has_team() {
    return team != null;
  }
  
  /**
   * Returns the team that the player is currently associated with.
   * This is the team that the player currently on during a match.
   *
   * @return Player's associated team.
   */
  public @Nullable WarTeam team() {
    return team;
  }
  
  /**
   * @param new_team the player is joining this team (consensually).
   */
  public void joins(WarTeam new_team) {
    this.team = new_team;
    
    changes_visibility();
    changes_name();
  }
  
  /**
   * players participating in the match should not be able to see spectators flying around.
   * spectators should be able to see everyone, but not interfere with the match participants at all.
   * <ul>
   *   <li>`setCollidable(false)` ensures spectators cannot bump match participants around.</li>
   * </ul>
   */
  private void changes_visibility() {
    if (has_team()) {
      player.setCollidable(true);
      // If they are playing, everyone can see this player.
      // They however, cannot see spectators.
      for (MiraPvpPlayer dp : mira.master().players().values())
        if (dp.equals(this)) continue;
        else if (dp.is_member_of_team()) {
          // They are both playing, so they can both see each other.
          dp.crafter().showPlayer(player);
          player.showPlayer(dp.crafter());
        } else {
          // The other player is spectating, so this player cannot see them.
          dp.crafter().showPlayer(player);
          player.hidePlayer(dp.crafter());
        }
    } else {
      player.setCollidable(false);
      // If they are spectating, only spectators can see this player.
      // They can see others playing as well.
      for (MiraPvpPlayer dp : manager.getWarPlayers().values())
        if (dp.equals(this)) continue;
        else if (dp.is_member_of_team()) {
          // The other player is playing, so they cannot see this player.
          dp.crafter().hidePlayer(player);
          player.showPlayer(dp.crafter());
        } else {
          // The other player is spectating, so they can see each other.
          dp.crafter().showPlayer(player);
          player.showPlayer(dp.crafter());
        }
    }
  }
  
  /**
   * Updates this player's display name.
   * This should be called whenever their
   * team changes or rank changes.
   */
  public void changes_name() {
    String prefix = "";
    if (manager.plugin().hasPermission(crafter(), "war.admin"))
      // Admins do not have any other prefixes, except map builder.
      prefix = ChatColor.GOLD + "@";
    else {
      if (manager.plugin().hasPermission(crafter(), "war.mod"))
        // Give mod prefixes priority too.
        prefix = ChatColor.DARK_PURPLE + "@";
      
      if (manager.plugin().hasPermission(crafter(), "war.donatorplus"))
        // DonatorPlus takes priority over Donator.
        prefix = ChatColor.YELLOW + "#" + prefix;
      else if (manager.plugin().hasPermission(crafter(), "war.donator"))
        // Otherwise do donator if they only have that.
        prefix = ChatColor.GREEN + "#" + prefix;
    }
    if (manager.cache().getCurrentMap().isCreator(crafter().getUniqueId()))
      prefix = ChatColor.DARK_RED + "#" + prefix;
    
    ChatColor teamColor = has_team() ? joins_team().getTeamColor() : ChatColor.LIGHT_PURPLE;
    crafter().setDisplayName(prefix + teamColor + name() + ChatColor.WHITE);
  }
}
