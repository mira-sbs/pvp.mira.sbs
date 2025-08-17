package sbs.mira.pvp.game.modes;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.stored.SerializedLocation;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.game.Gamemode;
import sbs.mira.pvp.MiraPvpMaster;
import sbs.mira.pvp.util.WoolColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * an extension to gamemode to implement koth.
 * created on 2017-04-23.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see MiraPulse
 * @since 1.0.0
 */
public class KoTH extends Gamemode {

    private WarTeam holder;
    private HashMap<String, Integer> captureTime;
    private HashMap<String, Integer> captures;
    private Location flag;
    private int interval;

    public void reset() {
        flag = null;
        holder = null;
        if (captureTime != null)
            captureTime.clear();
        captureTime = null;
    }

    public void initialize() {
        interval = 1;
        captureTime = new HashMap<>();
        captures = new HashMap<>();
        flag = ((SerializedLocation) map().attr().get("kothFlag")).toLocation(main.match().getCurrentWorld(), false);

        for (WarTeam team : getTeams()) {
            captureTime.put(team.getTeamName(), (Integer) map().attr().get("captureTime"));
            captures.put(team.getTeamName(), 0);
        }

        main.match().getCurrentWorld().getBlockAt(flag).setType(Material.WOOL);

        autoAssign();

        Objective obj = s().registerNewObjective("gm", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateScoreboard();

        for (Player online : Bukkit.getOnlinePlayers())
            online.setScoreboard(s());
    }

    public void tick() {
        if (holder != null) {
            int holdTime = captureTime.get(holder.getTeamName());
            captureTime.put(holder.getTeamName(), holdTime - 1);
            updateScoreboard();
            holdTime--;
            if (holdTime == 5) {
                logEvent(holder.getDisplayName() + " will win in 5 seconds!");
                Bukkit.broadcastMessage(holder.getDisplayName() + " will win in 5 seconds!");
            } else if (holdTime == 0) {
                onEnd();
                return;
            }
        }
        interval--;
        if (interval == 0) {
            interval = 4;
            doFireworks();
        }
    }

    public void onKill(MiraPlayer killed, MiraPlayer killer) {
    }

    public void onLeave(MiraPlayer left) {
    }

    public void onDeath(MiraPlayer killed) {
    }

    public void decideWinner() {
        int lowest = 999;
        ArrayList<WarTeam> winners = new ArrayList<>();

        for (WarTeam team : getTeams()) {
            int time = captureTime.get(team.getTeamName());
            if (time == lowest)
                winners.add(team);
            else if (time < lowest) {
                lowest = time;
                winners.clear();
                winners.add(team);
            }
        }
        broadcastWinner(winners, "seconds remaining", lowest);
    }

    /**
     * koth-specific procedure to spawn a firework at the flag.
     * if no one is holding it, spawn a white firework.
     * if a team is holding it, spawn a holding-team-colored firework.
     */
    private void doFireworks() {
        if (holder == null) // Spawn white.
            ((MiraPvpMaster) main).entity().spawnFirework(flag.clone().add(0.5, 1, 0.5), ChatColor.WHITE);
        else
            ((MiraPvpMaster) main).entity().spawnFirework(flag.clone().add(0.5, 1, 0.5), holder.getTeamColor());
    }

    public void updateScoreboard() {
        Objective obj = s().getObjective(DisplaySlot.SIDEBAR);

        String dp = map().getMapName() + " (" + getName() + ")";
        if (dp.length() > 32) dp = dp.substring(0, 32);
        obj.setDisplayName(dp);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(" ").setScore(captureTime.size() + 2);
        obj.getScore("  Time Remaining").setScore(captureTime.size() + 1);

        Iterator<WarTeam> iterator = getTeams().iterator();
        for (int i = 0; i < captureTime.size(); i++) {
            WarTeam target = iterator.next();
            obj.getScore(target.getTeamColor() + "    " + main.strings().getDigitalTime(captureTime.get(target.getTeamName()))).setScore(i + 1);
            s().resetScores(target.getTeamColor() + "    " + main.strings().getDigitalTime(captureTime.get(target.getTeamName()) + 1));
        }
        obj.getScore("  ").setScore(0);
    }

    public String getOffensive() {
        return "Break the wool in the middle of the map to control the flag!";
    }

    public String getDefensive() {
        return "Stop the enemy from controlling the flag if you have control!";
    }

    public String getFullName() {
        return "King of The Hill";
    }

    public String getName() {
        return "KoTH";
    }

    public String getGrammar() {
        return "a";
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        MiraPlayer wp = main.getWarPlayer(event.getPlayer());
        if (event.getBlock().getLocation().equals(flag)) {
            event.setCancelled(true);
            WarTeam target = wp.getCurrentTeam();
            if (target == null) return;
            if (holder == target)
                wp.crafter().sendMessage("You already have control of the flag!");
            else {
                Bukkit.broadcastMessage(wp.display_name() + " took the flag for " + target.getDisplayName() + "!");
                if (holder == null)
                    logEvent(wp.display_name() + " captured the flag first!");
                else
                    logEvent(wp.display_name() + " captured the flag for " + target.getDisplayName());
                holder = target;

                for (Player online : Bukkit.getOnlinePlayers())
                    online.playSound(online.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 1F, 1F);
                updateScoreboard();
                captures.put(target.getTeamName(), captures.get(target.getTeamName()) + 1);

                event.getBlock().getLocation().getBlock().setType(Material.WOOL);
                event.getBlock().getLocation().getBlock().setData(WoolColor.fromChatColor(target.getTeamColor()).getColor());
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().remove(flag.getBlock());
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.WOOL) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Capture Time", main.strings().getDigitalTime(captureTime.get(team.getTeamName())));
        extra.put("Flag Captures", captures.get(team.getTeamName()));
        return extra;
    }
}