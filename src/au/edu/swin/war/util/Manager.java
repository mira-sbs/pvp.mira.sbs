package au.edu.swin.war.util;

import au.edu.swin.war.Main;
import au.edu.swin.war.WarPlayerPlus;
import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.stats.WarStats;
import au.edu.swin.war.util.modules.ConfigUtility;
import au.edu.swin.war.util.modules.EntityUtility;
import au.edu.swin.war.util.modules.QueryUtility;
import au.edu.swin.war.util.modules.RespawnUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * An extension to WarManager.
 * Acts as a hub for all modules to interact.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarManager
 * <p>
 * Created by Josh on 20/04/2017.
 * @since 1.0
 */
public class Manager extends WarManager {

    private final Match matchutil; // An instance of the match controller.
    private final Cache cacheutil; // An instance of the cache.
    private final RespawnUtility respawnutil; // An instance of the respawning utility.
    private final EntityUtility entiutil; // An instance of the entity utility.
    private final ConfigUtility confutil; // An instance of the configuration utility.
    private final QueryUtility qryutil; // An instance of the database utility.

    private final ArrayList<UUID> warned; // Keeps track of warning messages for players.
    private final HashMap<UUID, WarStats> tempStats; // Holds statistics for a player until they log in.

    private ItemStack HANDBOOK; // Needs to be created in its own function because it's HELLA huge.

    /**
     * Creates an instance of this class.
     * Must be called in onEnable();
     *
     * @param plugin Instance of main plugin. Must extend WarPlugin.
     */
    public Manager(Main plugin) {
        super(plugin);
        // Initialize everything.
        this.cacheutil = new Cache(this);
        this.matchutil = new Match(this);
        this.respawnutil = new RespawnUtility(this);
        this.entiutil = new EntityUtility(this);
        this.confutil = new ConfigUtility(this);
        this.qryutil = new QueryUtility(this, plugin.getConfig().getBoolean("database.enabled"));
        new Guard(this); // Guard does not need a reference so just initialize it.
        new StatsListener(this); // Stats Listener does not need a reference so just initialize it.

        warned = new ArrayList<>();
        tempStats = new HashMap<>();
        // Task that allows players to receive a warning message every 3 seconds.
        // Clear warnings.
        Bukkit.getScheduler().runTaskTimer(plugin, warned::clear, 0L, 60L);
        createBook();
    }

    /**
     * Returns a running instance of the extended Match manager.
     *
     * @return Match manager.
     */
    public Match match() {
        return matchutil;
    }

    /**
     * Returns a running instance of the extended Cache.
     *
     * @return Cache.
     */
    public Cache cache() {
        return cacheutil;
    }

    /**
     * Returns a running instance of the respawn utility.
     *
     * @return Respawn utility.
     */
    public RespawnUtility respawn() {
        return respawnutil;
    }

    /**
     * Returns a running instance of the entity utility.
     *
     * @return Entity utility.
     */
    public EntityUtility entity() {
        return entiutil;
    }

    /**
     * Returns a running instance of the configuration utility.
     *
     * @return Configuration utility.
     */
    public ConfigUtility conf() {
        return confutil;
    }

    /**
     * Returns a running instance of the query utility.
     *
     * @return Query utility.
     */
    public QueryUtility query() {
        return qryutil;
    }

    /**
     * Temporarily holds onto a player's statistics
     * without necessarily having a WarPlayerPlus
     * instance created yet. (pre login)
     *
     * @param uuid      UUID associated with the stats.
     * @param tempStats Actual stats.
     */
    void putTempStats(UUID uuid, WarStats tempStats) {
        this.tempStats.put(uuid, tempStats);
    }

    /**
     * Creates an instance of a WarPlayer for a player.
     *
     * @param target The target to base the WarPlayer object on.
     */
    public WarPlayer craftWarPlayer(Player target) {
        WarStats stats = tempStats.getOrDefault(target.getUniqueId(), new WarStats(this, target.getUniqueId())); // Get their stats, or create new ones.
        tempStats.remove(target.getUniqueId()); // Remove their pre-login storage stats.
        WarPlayer result = new WarPlayerPlus(target, this, stats); // Create their instance.
        getWarPlayers().put(target.getUniqueId(), result); // Put it in the key/value set!
        return result;
    }

    /**
     * Checks if a player can be warned, and then warns them.
     *
     * @param whoWasWarned Who was warned. (lol)
     */
    public void warn(Player whoWasWarned, String warning) {
        if (warned.contains(whoWasWarned.getPlayer().getUniqueId())) return;
        warned.add(whoWasWarned.getPlayer().getUniqueId());
        whoWasWarned.sendMessage("TIP: " + warning);
    }

    /**
     * Returns a message from a selected key, and then replaces
     * the placeholders with relevant string data.
     *
     * @param key          Message key.
     * @param replacements Replacement phrases.
     * @return The resulting string.
     */
    public String _(String key, Object... replacements) {
        int i = 0;
        String result = conf().getMessage(key);
        while (result.contains("{" + i + "}")) {
            result = result.replaceAll("{" + i + "}", replacements[i].toString());
            i++;
        }
        return result;
    }

    /**
     * Gives a targeted player the spectator kit.
     * This isn't needed, but it might be later.
     *
     * @param wp The target player.
     * @since 1.0
     */
    @Deprecated /* See WarManager for deprecation reason */
    public void giveSpectatorKit(WarPlayer wp) {
        wp.getPlayer().getInventory().setHeldItemSlot(4);
        wp.getPlayer().getInventory().setItem(4, HANDBOOK);
    }

    /**
     * Creates the handbook because the process of
     * doing so consumes lines like no tomorrow.
     */
    private void createBook() {
        HANDBOOK = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) HANDBOOK.getItemMeta();
        bookMeta.setTitle(ChatColor.BOLD + "War: The Basics");
        bookMeta.setAuthor("War Administration");
        bookMeta.setGeneration(BookMeta.Generation.TATTERED);

        List<String> pages = new ArrayList<>();
        pages.add(ChatColor.translateAlternateColorCodes('&', "&lWar: The Basics\n&0Hey there, player!\n\nBook Contents:\n&ci.&0 Overview\n&9ii.&0 Commands\n&6iii.&0 Players\n&aiv.&0 Rules\n\nIf you're &cnew&0, read through me and then\n       &nHAVE FUN!\n\n&0  »»»"));
        pages.add(ChatColor.translateAlternateColorCodes('&', "&oPart I. An Overview\n&0Welcome to War!\n\nThis is a &5team-based &0strategy PvP server!\nWork with your &4team mates &0to win matches.\n\nThere's a &agamemode &0tosuit everyone's play style!\n\n\n&0     »»»"));
        pages.add(ChatColor.translateAlternateColorCodes('&', "&oPart II. Commands\n\nStart Playing!\n&c/join &0- &9/leave\n&0What's up next?\n&4/rotation\n&0Have your say!\n&a/vote &0<gamemode>\nStatistics!\n&6/stats &0+ &7/leaderboard\n\n&0Or, &n/? War\n\n&0        »»»"));
        pages.add(ChatColor.translateAlternateColorCodes('&', "&oPart III. Players\n&0You'll see these people online!\n\n&oStaff:\n&6@&8Administrator\n&5@&8Moderator\n\n&0&oOther Ranks:\n&a#&8Donator\n&e#&8DonatorPlus\n&4#&8MapCreator\n\n&0           »»»"));
        pages.add(ChatColor.translateAlternateColorCodes('&', "&oPart IV. Rules\n&0Follow these!\n\n&ci. &0Don't be a dick.\n&9ii. &0Play the game.\n&4iii. &0Don't cheat.\n&6iv. &0Don't combat log.\n&2v. &0Be a good sport.\n&5vi. &0Don't spawncamp.\n&8vii. &0Listen to @Staff\n&7viii. &0Have fun!\n\n\n&0              »»»"));
        pages.add(ChatColor.translateAlternateColorCodes('&', "&oNow, go get 'em!\n\n&0We encourage players to use &4common sense &0whilst playing. Have a safe, sensible, and &dfun &cWar!\n\n&0- Administration\n\n\n\n\n                  X"));
        bookMeta.setPages(pages);
        HANDBOOK.setItemMeta(bookMeta);
    }
}
