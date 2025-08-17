package sbs.mira.pvp.framework;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sbs.mira.pvp.framework.util.WarCache;
import sbs.mira.pvp.framework.util.WarMatch;
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
class MiraPluginMaster<M extends MiraPulse>
{
  
  private final M mira;
  private final TreeMap<UUID, MiraPlayer<?>> players;
  private final ItemUtility items;
  private final StringUtility strings;
  private final WorldUtility world;
  private final Random rng;
  
  public
  MiraPluginMaster(MiraPlugin plugin, MiraPulse mira, ItemUtility items, StringUtility strings, WorldUtility world)
  {
    this.mira = mira;
    this.items = items;
    this.strings = strings;
    this.world = world;
    this.players = new TreeMap<>();
    /*this.items = new ItemUtility(this);
    this.strings = new StringUtility(this);
    this.world = new WorldUtility(this);*/
    this.rng = new Random(0xfdffdeadL);
  }
  
  public
  M mira()
  {
    return mira;
  }
  
  /**
   * retrieve and prefill a pre-configured message template.
   *
   * @param key          message key name.
   * @param replacements replaces "{0}", "{1}" and so on with the provided.
   * @throws IllegalArgumentException message key does not exist.
   */
  public @NotNull
  String message(String key, String... replacements) throws IllegalArgumentException
  {
    int i = 0;
    String result = ChatColor.translateAlternateColorCodes('&', conf().getMessage(key));
    while (result.contains("{" + i + "}"))
    {
      result = result.replace("{%d}".formatted(i), replacements[i]);
      i++;
    }
    return result;
  }
  
  @NotNull
  public
  ItemUtility items()
  {
    return itemutil;
  }
  
  /**
   * Returns an instance of StringUtility so that
   * maps, gamemodes, etc. can quickly access functions
   * that allow Strings to be manipulated.
   *
   * @NotNull public StringUtility strings() {
   * return strutil;
   * }
   * <p>
   * /**
   * Returns an instance of WorldUtility so that
   * maps, gamemodes, etc. can quickly access functions
   * that allow world files to be manipulated.
   * @NotNull public WorldUtility world() {
   * return wrldutil;
   * }
   * <p>
   * /**
   * Returns a running instance of the match manager.
   * This cannot be held in the framework, so you will
   * need to create your own field and make this function
   * return the manager.
   * @NotNull public abstract WarMatch match();
   * <p>
   * /**
   * Returns a running instance of the cache manager.
   * This cannot be held in the framework, so you will
   * need to create your own field and make this function
   * return the cache.
   * @NotNull public abstract WarCache cache();
   * @see WarMatch
   * @see WarCache
   */
  
  @NotNull
  public
  Map<UUID, MiraPlayer<?>> players()
  {
    return players;
  }
  
  @NotNull
  public abstract
  MiraPlayer<?> declares(CraftPlayer target);
  
  /**
   * When called, this should clear a player's inventory
   * and if applicable, give the player a spectator kit.
   *
   * @param wp The target player.
   */
  public abstract
  void spectator(MiraPlayer wp);
  
  
  public
  void destroys(UUID victim)
  {
    players.remove(victim);
  }
  
  @Nullable
  public
  MiraPlayer<?> player(UUID target)
  {
    return players.get(target);
  }
  
  @Nullable
  public
  MiraPlayer<?> player(@Nullable Player target)
  {
    return target == null ? null : player(target.getUniqueId());
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
