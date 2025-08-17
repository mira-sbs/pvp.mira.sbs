package sbs.mira.pvp.framework;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.pvp.framework.util.modules.ItemUtility;
import sbs.mira.pvp.framework.util.modules.StringUtility;
import sbs.mira.pvp.framework.util.modules.WorldUtility;

import java.util.*;

/**
 * represents the state of a bukkit server under the influence of mira.
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @version 1.0.1
 * @see MiraPulse
 * @since 1.0.0
 */
public abstract
class MiraPluginMaster<Pulse extends MiraPulse<?, ?>, Player extends MiraPlayer<?>>
  implements Breather<Pulse>
{
  private @Nullable Pulse pulse;
  
  private final @NotNull Random rng;
  private final @NotNull TreeMap<UUID, Player> players;
  private final @NotNull ItemUtility items;
  private final @NotNull StringUtility strings;
  private final @NotNull WorldUtility world;
  
  public
  MiraPluginMaster()
  {
    this.rng = new Random(0xfdffdeadL);
    this.players = new TreeMap<>();
    this.items = new ItemUtility(this);
    this.strings = new StringUtility(this);
    this.world = new WorldUtility(this);
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
  
  public
  abstract
  @NotNull
  MiraPlayer<?> declares(CraftPlayer target);
  
  /**
   * When called, this should clear a player's inventory
   * and if applicable, give the player a spectator kit.
   *
   * @param wp The target player.
   */
  public
  abstract
  void spectating(Player wp);
  
  
  public
  void destroys(UUID victim)
  {
    players.remove(victim);
  }
  
  public
  @Nullable
  Player player(UUID target)
  {
    return players.get(target);
  }
  
  @Nullable
  public
  Player player(@Nullable Player target)
  {
    return target == null ? null : player(target.getUniqueId());
  }
  
  public
  @NotNull
  Map<UUID, Player> players()
  {
    return players;
  }
  
  public
  @NotNull
  ItemUtility items()
  {
    return items;
  }
  
  public
  @NotNull
  StringUtility strings()
  {
    return strings;
  }
  
  public
  @NotNull
  WorldUtility world()
  {
    return world;
  }
  
  /**
   * retrieve and prefill a pre-configured message template.
   *
   * @param key          message key name.
   * @param replacements replaces "{0}", "{1}" and so on with the provided.
   * @throws IllegalArgumentException message key does not exist.
   */
  public
  @NotNull
  String message(String key, String... replacements) throws IllegalArgumentException
  {
    int i = 0;
    String result = ChatColor.translateAlternateColorCodes('&', pulse().conf().getMessage(key));
    while (result.contains("{" + i + "}"))
    {
      result = result.replace("{%d}".formatted(i), replacements[i]);
      i++;
    }
    return result;
  }
  
  /**
   * Sends a TextComponent message to everyone online.
   *
   * @param comp Message to send.
   */
  public
  void broadcastSpigotMessage(TextComponent comp)
  {
    for (MiraPlayer<?> online : players.values())
    {
      online.crafter().spigot().sendMessage(comp);
    }
  }
}
