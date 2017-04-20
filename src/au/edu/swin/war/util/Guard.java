package au.edu.swin.war.util;

import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.framework.util.WarMatch;
import au.edu.swin.war.framework.util.WarModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * This class listens for certain Spigot events in
 * certain scenarios and blocks them.
 * <p>
 * Created by Josh on 20/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @since 1.0
 */
public class Guard extends WarModule implements Listener {

    protected Guard(WarManager main) {
        super(main);
        main().plugin().getServer().getPluginManager().registerEvents(this, main().plugin());
    }

    /* All events below prevent damage/interaction out of play time. */

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (main().match().getStatus() != WarMatch.Status.PLAYING)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (main().match().getStatus() != WarMatch.Status.PLAYING)
            event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (main().match().getStatus() != WarMatch.Status.PLAYING)
            event.setCancelled(true);
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (main().match().getStatus() != WarMatch.Status.PLAYING)
            event.setCancelled(true);
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (main().match().getStatus() != WarMatch.Status.PLAYING)
            event.setCancelled(true);
    }
}
