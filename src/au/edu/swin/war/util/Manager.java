package au.edu.swin.war.util;

import au.edu.swin.war.Main;
import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.util.modules.ConfigUtility;
import au.edu.swin.war.util.modules.EntityUtility;
import au.edu.swin.war.util.modules.RespawnUtility;
import org.bukkit.entity.Player;

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
        new Guard(this); // Guard does not need a reference so just initialize it.
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
     * Creates an instance of a WarPlayer for a player.
     *
     * @param target The target to base the WarPlayer object on.
     */
    public WarPlayer craftWarPlayer(Player target) {
        WarPlayer result = new WarPlayer(target, this); // Create their instance.
        getWarPlayers().put(target.getUniqueId(), result); // Put it in the key/value set!
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

    }
}
