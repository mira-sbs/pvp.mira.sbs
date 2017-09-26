package au.edu.swin.war;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.stats.WarStats;
import au.edu.swin.war.util.Manager;
import org.bukkit.entity.Player;

/**
 * An extension to WarPlayer that contains non-framework fields.
 * Mostly used for statistics tracking.
 *
 * @author ILavaYou
 * @version 1.0
 * @since 1.1
 */
public final class WarPlayerPlus extends WarPlayer {

    private final WarStats stats; // Stats object.

    /**
     * Constructor for WarPlayer class.
     *
     * @param player  The Spigot instance of the Player's entity.
     * @param manager Instance of the supercontroller.
     */
    public WarPlayerPlus(Player player, Manager manager, WarStats stats) {
        super(player, manager);
        this.stats = stats;
    }

    /**
     * Returns this player's statistics record.
     *
     * @return Statistics for this player.
     */
    public WarStats stats(){
        return stats;
    }
}
