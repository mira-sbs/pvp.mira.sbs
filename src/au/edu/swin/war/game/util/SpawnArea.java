package au.edu.swin.war.game.util;

import au.edu.swin.war.event.PostMatchPlayerRespawnEvent;
import au.edu.swin.war.framework.event.MatchPlayerJoinEvent;
import au.edu.swin.war.framework.event.MatchPlayerLeaveEvent;
import au.edu.swin.war.framework.stored.Activatable;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.framework.util.WarModule;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class can be created to serve the simple purpose
 * of protecting the team players within it. It stops anyone
 * who has respawned within it from taking damage until they
 * leave, at which point the protection wears off forever.
 * <p>
 * It also stops blocks from being modified in this area.
 * <p>
 * Created by Josh on 30/08/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.1
 * @since 1.1
 */
public class SpawnArea extends WarModule implements Activatable, Listener {

    private int x1, z2, z1, x2;
    private boolean arrows;
    private boolean reEntry;
    private List<UUID> invincible;
    private List<UUID> notified;

    /**
     * Spawn area constructor.
     *
     * @param main                       Supercontroller for listener registration.
     * @param x1                         Bottom left X.
     * @param z1                         Bottom left Z.
     * @param x2                         Top right X.
     * @param z2                         Top right Z.
     * @param allowArrowsWhilstProtected Allows protected players to shoot arrows.
     * @param allowReEntry               Allows players to (re)enter this area.
     */
    public SpawnArea(WarManager main, int x1, int z1, int x2, int z2, boolean allowArrowsWhilstProtected, boolean allowReEntry) {
        super(main);
        this.x1 = Math.min(x1, x2);
        this.z1 = Math.min(z1, z2);
        this.x2 = Math.max(x1, x2);
        this.z2 = Math.max(z1, z2);
        this.arrows = allowArrowsWhilstProtected;
        this.reEntry = allowReEntry;
        invincible = new ArrayList<>();
        notified = new ArrayList<>();
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
        main().plugin().getServer().getPluginManager().registerEvents(this, main().plugin());
    }

    @Override
    public void deactivate() {
        invincible.clear();
        notified.clear();
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onCustomRespawn(PostMatchPlayerRespawnEvent event) {
        if (isInside(event.getPlayer().getPlayer().getLocation())) {
            invincible.add(event.getPlayer().getPlayer().getUniqueId());
            if (!notified.contains(event.getPlayer().getPlayer().getUniqueId()))
                event.getPlayer().sendMessage("TIP: You are now spawn protected.");
        }
    }

    @EventHandler
    public void onJoin(MatchPlayerJoinEvent event) {
        if (isInside(event.getPlayer().getPlayer().getLocation())) {
            invincible.add(event.getPlayer().getPlayer().getUniqueId());
            if (!notified.contains(event.getPlayer().getPlayer().getUniqueId()))
                event.getPlayer().sendMessage("TIP: You are now spawn protected.");
        }
    }

    @EventHandler
    public void onLeave(MatchPlayerLeaveEvent event) {
        if (invincible.contains(event.getPlayer().getPlayer().getUniqueId()))
            invincible.remove(event.getPlayer().getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isInside(event.getTo())) {
            if (invincible.contains(event.getPlayer().getUniqueId())) {
                invincible.remove(event.getPlayer().getUniqueId());
                if (!notified.contains(event.getPlayer().getUniqueId())) {
                    event.getPlayer().sendMessage("You are no longer spawn protected.");
                    notified.add(event.getPlayer().getUniqueId());
                }
            }
        } else if (!reEntry && event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            if (isInside(event.getFrom())) return;
            event.setCancelled(true);
            event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(-0.75));
        }
    }

    @EventHandler
    public void onShoot(ProjectileLaunchEvent event) {
        if (!arrows)
            if (event.getEntity().getShooter() instanceof Player)
                if (invincible.contains(((Player) event.getEntity().getShooter()).getUniqueId())) {
                    event.setCancelled(true);
                    ((Player) event.getEntity().getShooter()).sendMessage("You cannot shoot whilst you are protected.");
                }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (invincible.contains(event.getEntity().getUniqueId()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (isInside(event.getBlock().getLocation()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (isInside(event.getBlock().getLocation()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        List<Block> toKeep = new ArrayList<>();
        for (Block block : event.blockList())
            if (isInside((block.getLocation())))
                toKeep.add(block);
        event.blockList().removeAll(toKeep);
    }
}
