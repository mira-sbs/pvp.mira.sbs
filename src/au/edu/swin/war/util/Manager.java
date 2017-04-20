package au.edu.swin.war.util;

import au.edu.swin.war.Main;
import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.util.WarManager;
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
    }

    private Match matchutil; // An instance of the match controller.
    private Cache cacheutil; // An instance of the cache.
    private RespawnUtility respawnutil; // An instance of the respawning utility.


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
     * Creates an instance of a WarPlayer for a player.
     *
     * @param target The target to base the WarPlayer object on.
     */
    public void craftWarPlayer(Player target) {

    }

    /**
     * Gives a targeted player the spectator kit.
     * This isn't needed, but it might be later.
     *
     * @param wp The target player.
     * @since 1.0
     */
    public void giveSpectatorKit(WarPlayer wp) {

    }
}
