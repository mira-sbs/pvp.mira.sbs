package sbs.mira.pvp.game.util;

import sbs.mira.pvp.framework.stored.Activatable;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.framework.MiraModule;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * This class can be used to prevent team-grief to a degree.
 * <p>
 * Created by Josh on 31/08/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.1
 * @since 1.1
 */
public class AntiTeamGrief extends MiraModule implements Activatable, Listener {

    private final int x1;
    private final int z2;
    private final int z1;
    private final int x2;
    private final ChatColor teamColor;
    private final boolean penalize;

    /**
     * Spawn area constructor.
     *
     * @param main     Supercontroller for listener registration.
     * @param x1       Bottom left X.
     * @param z1       Bottom left Z.
     * @param x2       Top right X.
     * @param z2       Top right Z.
     * @param penalize Penalize whoever tries to team-grief.
     */
    public AntiTeamGrief(MiraPulse main, ChatColor teamColor, int x1, int z1, int x2, int z2, boolean penalize) {
        super(main);
        this.teamColor = teamColor;
        this.x1 = Math.min(x1, x2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.z2 = Math.max(z1, z2);
        this.penalize = penalize;
    }

    /**
     * Returns true if a location is inside the defined square area.
     *
     * @param loc Location to compare.
     * @return Is it inside?
     */
    private boolean isInside(Location loc) {
        return loc.getBlockX() >= x1 && loc.getBlockX() <= x2 && loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2;
    }

    @Override
    public void activate() {
        mira().plugin().getServer().getPluginManager().registerEvents(this, mira().plugin());
    }

    @Override
    public void deactivate() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.TNT)
            if (isInside(event.getBlock().getLocation()))
                if (mira().getWarPlayer(event.getPlayer().getUniqueId()).getCurrentTeam().getTeamColor().equals(teamColor)) {
                    mira().warn(event.getPlayer(), "Place TNT inside the enemy area!");
                    event.setCancelled(true);
                    if (penalize)
                        event.getPlayer().damage(event.getPlayer().getHealth() <= 5 ? event.getPlayer().getHealth() - 1 : 5);
                }
    }
}
