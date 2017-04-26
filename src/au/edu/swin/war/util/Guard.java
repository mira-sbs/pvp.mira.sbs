package au.edu.swin.war.util;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.framework.util.WarMatch;
import au.edu.swin.war.framework.util.WarModule;
import au.edu.swin.war.game.Map;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * This class listens for certain Spigot events in
 * certain scenarios and blocks/acts upon them.
 * <p>
 * Created by Josh on 20/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @since 1.0
 */
public class Guard extends WarModule implements Listener {

    Guard(WarManager main) {
        super(main);
        main().plugin().getServer().getPluginManager().registerEvents(this, main().plugin());
    }

    /**
     * This event procedure handles high-priority logic
     * when a player first connects to the server.
     *
     * @param event An event called by Spigot.
     */
    @EventHandler(priority = EventPriority.HIGHEST) // Highest priority denoting this one needs to be executed first.
    public void onJoin(PlayerJoinEvent event) {
        Player target = event.getPlayer(); // Get the player who connected.
        WarPlayer wp = main().craftWarPlayer(target); // Creates their needed WarPlayer record.

        WarMatch.Status status = main().match().getStatus(); // Get the status of the match.
        // Clear the player's inventory and give them the spectator kit.
        main().items().clear(wp);
        main().giveSpectatorKit(wp);

        if (status == WarMatch.Status.STARTING || status == WarMatch.Status.PLAYING || status == WarMatch.Status.CYCLE)
            target.teleport(main().cache().getCurrentMap().getSpectatorSpawn()); // Spawn them in the current defined map.
        else if (status == WarMatch.Status.VOTING)
            target.teleport(((Map) main().cache().getMap(main().match().getPreviousMap())).getSpectatorSpawn_()); // Spawn them in the previous defined map.

        if (status != WarMatch.Status.PLAYING) {
            event.getPlayer().setScoreboard(((Match) main().match()).s()); // Show the default scoreboard.
            ((Match) main().match()).s().getTeam("PostSpectators").addEntry(event.getPlayer().getName()); // Add them to this scoreboard.
            //TODO: Add them as spectators???
            target.setGameMode(GameMode.CREATIVE);
        } else {
            event.getPlayer().setScoreboard(main().match().getCurrentMode().s()); // Show the gamemode's scoreboard.
            target.setGameMode(GameMode.SPECTATOR);
        }
    }

    /**
     * This event procedure correctly handles what
     * happens when a player disconnects.
     *
     * @param event An event called by Spigot.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.getPlayer().performCommand("leave"); // Act as if they were using the leave command.
        main().destroyWarPlayer(event.getPlayer().getUniqueId()); // Remove their WarPlayer record.
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
        else if (event.getEntity() instanceof Player)
            if (((Player) event.getEntity()).getGameMode() == GameMode.SPECTATOR)
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
