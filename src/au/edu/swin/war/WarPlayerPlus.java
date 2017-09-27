package au.edu.swin.war;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.stats.WarStats;
import au.edu.swin.war.util.Manager;
import org.bukkit.ChatColor;
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
    public WarStats stats() {
        return stats;
    }

    /**
     * Updates this player's display name.
     * This should be called whenever their
     * team changes or rank changes.
     */
    public void update() {
        String prefix = "";
        if (manager.plugin().hasPermission(getPlayer(), "war.admin"))
            // Admins do not have any other prefixes, except map builder.
            prefix = ChatColor.GOLD + "@";
        else {
            if (manager.plugin().hasPermission(getPlayer(), "war.mod"))
                // Give mod prefixes priority too.
                prefix = ChatColor.DARK_PURPLE + "@";

            if (manager.plugin().hasPermission(getPlayer(), "war.donatorplus"))
                // DonatorPlus takes priority over Donator.
                prefix = ChatColor.YELLOW + "#" + prefix;
            else if (manager.plugin().hasPermission(getPlayer(), "war.donator"))
                // Otherwise do donator if they only have that.
                prefix = ChatColor.GREEN + "#" + prefix;
        }
        if (manager.cache().getCurrentMap().isCreator(getPlayer().getUniqueId()))
            prefix = ChatColor.DARK_RED + "#" + prefix;

        ChatColor teamColor = isPlaying() ? getCurrentTeam().getTeamColor() : ChatColor.LIGHT_PURPLE;
        getPlayer().setDisplayName(prefix + teamColor + getName() + ChatColor.WHITE);
    }
}
