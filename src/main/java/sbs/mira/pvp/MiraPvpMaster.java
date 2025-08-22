package sbs.mira.pvp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPluginMaster;
import sbs.mira.pvp.stats.WarStats;
import sbs.mira.pvp.util.Cache;
import sbs.mira.pvp.util.Match;
import sbs.mira.pvp.util.modules.EntityUtility;
import sbs.mira.pvp.util.modules.QueryUtility;
import sbs.mira.pvp.util.modules.RespawnUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * [wit.]
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see sbs.mira.core
 * @since 1.0.0
 */
public
class MiraPvpMaster
  extends MiraPluginMaster<MiraPvpPulse, MiraPvpPlayer>
{
  
  private final Match match;
  private final Cache cache;
  private final RespawnUtility respawn;
  private final EntityUtility entities;
  private final MiraConfiguration config;
  private final QueryUtility db;
  
  private final ArrayList<UUID> warned; // Keeps track of warning messages for players.
  private final HashMap<UUID, WarStats> tempStats;
  
  private ItemStack HANDBOOK;
  ItemStack VOTE;
  ItemStack SKYBLOCK;
  
  public
  MiraPvpMaster(MiraPvpPlugin plugin)
  {
    this.match = new Match(this);
    this.cacheutil = new Cache(this);
    this.respawnutil = new RespawnUtility(this);
    this.entiutil = new EntityUtility(this);
    this.qryutil = new QueryUtility(this, plugin.getConfig().getBoolean("database.enabled"));
    new Guard(this);
    new StatsListener(this);
    
    warned = new ArrayList<>();
    tempStats = new HashMap<>();
    Bukkit.getScheduler().runTaskTimer(plugin, warned::clear, 0L, 60L);
    createItems();
  }
  
  /**
   * returns a running instance of the extended Match manager.
   *
   * @return match manager.
   */
  public
  Match match()
  {
    return matchutil;
  }
  
  /**
   * returns a running instance of the extended Cache.
   *
   * @return cache.
   */
  public
  Cache cache()
  {
    return cacheutil;
  }
  
  /**
   * returns a running instance of the respawn utility.
   *
   * @return respawn utility.
   */
  public
  RespawnUtility respawn()
  {
    return respawnutil;
  }
  
  /**
   * returns a running instance of the entity utility.
   *
   * @return entity utility.
   */
  public
  EntityUtility entity()
  {
    return entiutil;
  }
  
  /**
   * returns a running instance of the configuration utility.
   *
   * @return configuration utility.
   */
  public
  MiraConfiguration conf()
  {
    return confutil;
  }
  
  /**
   * returns a running instance of the query utility.
   *
   * @return query utility.
   */
  public
  QueryUtility query()
  {
    return qryutil;
  }
  
  /**
   * temporarily holds onto a player's statistics
   * without necessarily having a WarPlayerPlus
   * instance created yet. (pre login)
   *
   * @param uuid      uuid associated with the stats.
   * @param tempStats actual stats.
   */
  void putTempStats(UUID uuid, WarStats tempStats)
  {
    this.tempStats.put(uuid, tempStats);
  }
  
  /**
   * creates an instance of a WarPlayer for a player.
   *
   * @param target the target to base the WarPlayer object on.
   */
  public
  MiraPvpPlayer declares(Player target)
  {
    WarStats stats = tempStats.getOrDefault(target.getUniqueId(), new WarStats(this, target.getUniqueId()));
    tempStats.remove(target.getUniqueId()); // Remove their pre-login storage stats.
    MiraPlayer result = new MiraPvpPlayer(target.getHan, this, stats); // Create their instance.
    players().put(target.getUniqueId(), result); // Put it in the key/value set!
    return result;
  }
  
  /**
   * checks if a player can be warned, and then warns them.
   *
   * @param whoWasWarned who was warned.
   */
  public
  void warn(Player whoWasWarned, String warning)
  {
    if (warned.contains(whoWasWarned.getPlayer().getUniqueId()))
    {
      return;
    }
    warned.add(whoWasWarned.getPlayer().getUniqueId());
    whoWasWarned.sendMessage("TIP: " + warning);
  }
  
  /**
   * gives a targeted player the spectator kit.
   * this isn't needed, but it might be later.
   *
   * @param wp the target player.
   * @since 1.0
   */
  public
  void spectating(MiraPlayer wp)
  {
    wp.crafter().getInventory().setHeldItemSlot(4);
    wp.crafter().getInventory().setItem(4, HANDBOOK);
    wp.crafter().getInventory().setItem(0, SKYBLOCK);
    wp.crafter().getInventory().setItem(1, VOTE);
  }
  
  /**
   * creates the handbook because the process of
   * doing so consumes lines like no tomorrow.
   * all other items too why not.
   */
  private
  void createItems()
  {
    HANDBOOK = new ItemStack(Material.WRITTEN_BOOK);
    BookMeta bookMeta = (BookMeta) HANDBOOK.getItemMeta();
    bookMeta.setTitle(ChatColor.BOLD + "War: The Basics");
    bookMeta.setAuthor("War Administration");
    bookMeta.setGeneration(BookMeta.Generation.TATTERED);
    
    List<String> pages = new ArrayList<>();
    pages.add(ChatColor.translateAlternateColorCodes(
      '&',
      "&lWar: The Basics\n&0Hey there, player!\n\nBook Contents:\n&ci.&0 Overview\n&9ii.&0 Commands\n&6iii.&0 Players\n&aiv.&0 Rules\n\nIf you're &cnew&0, read through me and then\n       &nHAVE FUN!\n\n&0  »»»"
    ));
    pages.add(ChatColor.translateAlternateColorCodes(
      '&',
      "&oPart I. An Overview\n&0Welcome to War!\n\nThis is a &5team-based &0strategy PvP server!\nWork with your &4team mates &0to win matches.\n\nThere's a &agamemode &0tosuit everyone's play style!\n\n\n&0     »»»"
    ));
    pages.add(ChatColor.translateAlternateColorCodes(
      '&',
      "&oPart II. Commands\n\nStart Playing!\n&c/join &0- &9/leave\n&0What's up next?\n&4/rotation\n&0Have your say!\n&a/vote &0<gamemode>\nStatistics!\n&6/stats &0+ &7/leaderboard\n\n&0Or, &n/? War\n\n&0        »»»"
    ));
    pages.add(ChatColor.translateAlternateColorCodes(
      '&',
      "&oPart III. Players\n&0You'll see these people online!\n\n&oStaff:\n&6@&8Administrator\n&5@&8Moderator\n\n&0&oOther Ranks:\n&a#&8Donator\n&e#&8DonatorPlus\n&4#&8MapCreator\n\n&0           »»»"
    ));
    pages.add(ChatColor.translateAlternateColorCodes(
      '&',
      "&oPart IV. Rules\n&0Follow these!\n\n&ci. &0Don't be a dick.\n&9ii. &0Play the game.\n&4iii. &0Don't cheat.\n&6iv. &0Don't combat log.\n&2v. &0Be a good sport.\n&5vi. &0Don't spawncamp.\n&8vii. &0Listen to @Staff\n&7viii. &0Have fun!\n\n\n&0              »»»"
    ));
    pages.add(ChatColor.translateAlternateColorCodes(
      '&',
      "&oNow, go get 'em!\n\n&0We encourage players to use &4common sense &0whilst playing. Have a safe, sensible, and &dfun &cWar!\n\n&0- Administration\n\n\n\n\n                  X"
    ));
    bookMeta.setPages(pages);
    HANDBOOK.setItemMeta(bookMeta);
    
    SKYBLOCK = new ItemStack(Material.EYE_OF_ENDER);
    ItemMeta sbMeta = SKYBLOCK.getItemMeta();
    sbMeta.setDisplayName(ChatColor.BOLD + "To: Skyblock");
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.GRAY + "Right click to return");
    lore.add(ChatColor.GRAY + "to the Skyblock server!");
    sbMeta.setLore(lore);
    SKYBLOCK.setItemMeta(sbMeta);
    
    VOTE = items().createPotion(PotionEffectType.HEAL, 0, 0, 1);
    ItemMeta voteMeta = VOTE.getItemMeta();
    voteMeta.setDisplayName(ChatColor.BOLD + "Vote For Us!");
    lore.clear();
    lore.add(ChatColor.GRAY + "Right click to reaveal the");
    lore.add(ChatColor.GRAY + "voting link. Earn rewards!");
    voteMeta.setLore(lore);
    VOTE.setItemMeta(voteMeta);
  }
  
  @Override
  public @NotNull
  Cache observe()
  {
    return cacheutil;
  }
  
  @Override
  public @NotNull
  MiraPlayer<?> declares(@NotNull CraftPlayer target)
  {
    return null;
  }
  
  @Override
  public
  void spectating(@NotNull MiraPvpPlayer wp)
  {
  
  }
}
