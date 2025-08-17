package sbs.mira.pvp.game.modes;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.stored.SerializedLocation;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.game.Gamemode;
import sbs.mira.pvp.MiraPvpMaster;
import sbs.mira.pvp.util.WoolColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * an extension to gamemode to implement ctf.
 * created on 2017-04-24.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see MiraPulse
 * @since 1.0.0
 */
public class CTF extends Gamemode {

    private HashMap<String, CTFInfo> info;
    private HashMap<String, String> capture;
    private boolean instantBreak;
    private int interval = 1;

    public void reset() {
        if (capture != null)
            capture.clear();
        capture = null;
        if (info != null)
            info.clear();
        info = null;
        instantBreak = false;
    }

    public void initialize() {
        interval = 1;
        info = new HashMap<>();
        capture = new HashMap<>();

        for (WarTeam team : getTeams())
            info.put(team.getTeamName(), new CTFInfo(team));

        autoAssign();
        restoreFlags();
        
        Objective obj = s().registerNewObjective("gm", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateScoreboard();

        for (Player online : Bukkit.getOnlinePlayers())
            online.setScoreboard(s());
    }

    public void tick() {
        interval--;
        if (interval == 0) {
            interval = 4;
            doFireworks();
        }
        if (getTimeElapsed() > (150 * (Integer) map().attr().get("captureRequirement")) && !instantBreak) {
            instantBreak = true;
            Bukkit.broadcastMessage("This match is taking too long, Instant Break is now enabled!");
            logEvent("Instant break was enabled!");
        }
    }

    public void onKill(MiraPlayer killed, MiraPlayer killer) {
        dropFlag(killed);
    }

    public void onDeath(MiraPlayer dead) {
        dropFlag(dead);
    }

    /**
     * if the player is holding a flag and they die,
     * drop the flag, restore it, and broadcast it.
     * this is called from both classes since they are
     * functionally identical except one has a killer.
     *
     * @param killed The player who died.
     */
    private void dropFlag(MiraPlayer killed) {
        for (CTFInfo inf : info.values()) {
            if (inf.getHolder() != null)
                if (inf.getHolder().equals(killed.name())) {
                    capture.remove(killed.name());
                    Bukkit.broadcastMessage(killed.display_name() + " dropped " + inf.getTeam().getDisplayName() + "'s flag!");
                    logEvent(killed.display_name() + " dropped " + inf.getTeam().getDisplayName() + "'s flag!");
                    for (Player target : Bukkit.getOnlinePlayers())
                        target.playSound(target.getLocation(), Sound.ENTITY_IRONGOLEM_HURT, 1F, 1F);
                    inf.setHolder(null);
                    restoreFlags();
                    updateScoreboard();
                }
        }
    }

    public void decideWinner() {
        int highest = -1;
        ArrayList<WarTeam> winners = new ArrayList<>();

        for (WarTeam team : getTeams()) {
            int count = info.get(team.getTeamName()).getCaptures();
            if (count == highest)
                winners.add(team);
            else if (count > highest) {
                highest = count;
                winners.clear();
                winners.add(team);
            }
        }
        broadcastWinner(winners, "captures", highest);
    }

    public void onLeave(MiraPlayer left) {
        dropFlag(left);
    }

    /**
     * spawns fireworks at each flag or flag holder location.
     */
    private void doFireworks() {
        for (CTFInfo inf : info.values())
            if (inf.getHolder() == null)
                ((MiraPvpMaster) main).entity().spawnFirework(inf.getFlag().clone().add(0.5, 1, 0.5), inf.getTeam().getTeamColor());
            else
                ((MiraPvpMaster) main).entity().spawnFirework(Bukkit.getPlayer(inf.getHolder()).getLocation(), inf.getTeam().getTeamColor());
    }

    /**
     * restores any flags to their pedestals if
     * they are currently not being held.
     */
    private void restoreFlags() {
        for (CTFInfo inf : info.values())
            if (inf.getHolder() == null) {
                Block flag = inf.flag.getBlock();
                flag.setType(Material.WOOL);
                flag.setData(WoolColor.fromChatColor(inf.target.getTeamColor()).getColor());
            }
    }

    public void updateScoreboard() {
        Objective obj = s().getObjective(DisplaySlot.SIDEBAR);

        String dp = map().getMapName() + " (" + getName() + ")";
        if (dp.length() > 32) dp = dp.substring(0, 32);
        obj.setDisplayName(dp);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        obj.getScore(" ").setScore(info.size() + 2);
        obj.getScore("  Captures").setScore(info.size() + 1);

        int rqmt = (int) map().attr().get("captureRequirement");
        Iterator<WarTeam> iterator = getTeams().iterator();
        for (int i = 0; i < info.size(); i++) {
            CTFInfo inf = info.get(iterator.next().getTeamName());
            if (inf.getHolder() == null) {
                obj.getScore(inf.getTeam().getTeamColor() + "    █ " + inf.getCaptures() + ChatColor.GRAY + "/" + rqmt).setScore(i + 1);
                s().resetScores(inf.getTeam().getTeamColor() + "    ▓ " + inf.getCaptures() + ChatColor.GRAY + "/" + rqmt);
                s().resetScores(inf.getTeam().getTeamColor() + "    █ " + (inf.getCaptures() - 1) + ChatColor.GRAY + "/" + rqmt);
                s().resetScores(inf.getTeam().getTeamColor() + "    ▓ " + (inf.getCaptures() - 1) + ChatColor.GRAY + "/" + rqmt);
            } else {
                obj.getScore(inf.getTeam().getTeamColor() + "    ▓ " + inf.getCaptures() + ChatColor.GRAY + "/" + rqmt).setScore(i + 1);
                s().resetScores(inf.getTeam().getTeamColor() + "    █ " + inf.getCaptures() + ChatColor.GRAY + "/" + rqmt);
            }
        }
        obj.getScore("  ").setScore(0);
    }


    public String getOffensive() {
        return "Steal the other enemy's flag and capture it by punching your flag!";
    }

    public String getDefensive() {
        return "Stop the enemy from taking your flag!";
    }

    public String getFullName() {
        return "Capture The Flag";
    }

    public String getName() {
        return "CTF";
    }

    public String getGrammar() {
        return "a";
    }

    /**
     * check if a win has been attained after a capture.
     * if there is a win, onEnd should be called.
     *
     * @return whether or not any team has won.
     */
    private boolean checkWin() {
        for (CTFInfo inf : info.values())
            if (inf.getCaptures() >= (Integer) map().attr().get("captureRequirement"))
                return true;
        return false;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BEDROCK) return;
        MiraPlayer wp = main.getWarPlayer(event.getPlayer());
        if (checkBreak(wp, event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPunch(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        MiraPlayer wp = main.getWarPlayer(event.getPlayer().getUniqueId());
        if (capture.containsKey(wp.name()))
            for (CTFInfo inf : info.values()) {
                if (inf.flag.equals(event.getClickedBlock().getLocation())) {
                    if (inf.target.getTeamName().equals(wp.getCurrentTeam().getTeamName())) {
                        if (inf.getHolder() == null) {

                            for (Player target : Bukkit.getOnlinePlayers())
                                target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
                            capture.remove(wp.name());
                            inf.addCapture();
                            for (CTFInfo inf2 : info.values())
                                if (inf2.getHolder() != null)
                                    if (inf2.getHolder().equals(wp.name())) {
                                        Bukkit.broadcastMessage(wp.display_name() + " captured " + inf2.getTeam().getDisplayName() + "'s flag!");
                                        logEvent(wp.display_name() + " captured " + inf2.getTeam().getDisplayName() + "'s flag");
                                        inf2.setHolder(null);
                                        break;
                                    }
                            restoreFlags();
                            updateScoreboard();
                            if (checkWin()) {
                                onEnd();
                                break;
                            }
                        } else
                            main.warn(wp.crafter(), inf.getHolder() + " is holding your team's flag. You cannot capture!");
                    }
                }
            }
        else if (instantBreak) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                checkBreak(wp, event.getClickedBlock());
            }
        }
    }

    /**
     * checks if a block broken was a flag.
     * this also applies to instant capture mode.
     *
     * @param wp     the player who broke a block.
     * @param broken the block broken.
     * @return whether the event needs to be cancelled or not.
     */
    private boolean checkBreak(MiraPlayer wp, Block broken) {
        if (!wp.is_member_of_team()) return false;
        for (CTFInfo inf : info.values()) {
            if (inf.flag.equals(broken.getLocation())) {
                if (capture.containsKey(wp.name())) {
                    main.warn(wp.crafter(), "You can't steal more than one flag at once!");
                    return true;
                }
                if (wp.getCurrentTeam().getTeamName().equals(inf.target.getTeamName())) {
                    main.warn(wp.crafter(), "You can't steal your own flag! Defend it!");
                    return true;
                }
                inf.setHolder(wp.name());

                capture.put(wp.name(), inf.target.getTeamColor() + inf.target.getTeamName());
                info.get(wp.getCurrentTeam().getTeamName()).addAttempt();

                Bukkit.broadcastMessage(wp.display_name() + " has stolen " + inf.getTeam().getDisplayName() + "'s flag!");
                logEvent(wp.display_name() + " has stolen " + inf.getTeam().getDisplayName() + "'s flag");

                for (Player target : Bukkit.getOnlinePlayers())
                    target.playSound(target.getLocation(), Sound.ENTITY_ARROW_HIT, 1F, 1F);
                broken.setType(Material.BEDROCK);
                updateScoreboard();
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.WOOL) {
            event.setCancelled(true);
            event.getItem().remove();
        }
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Flag Captures", info.get(team.getTeamName()).getCaptures());
        extra.put("Flag Steals", info.get(team.getTeamName()).getAttempts());
        return extra;
    }

    /**
     * private record to hold a list of CTF information for a team.
     * this class holds:
     * -> the team associated with it.
     * -> the location of their flag.
     * -> the holder of their flag, if any.
     * -> the amount of captures they've made.
     * -> the amount of flag steals they've made.
     */
    private class CTFInfo {
        final WarTeam target;
        final Location flag;
        String holder;
        int captures;
        int attempts;

        CTFInfo(WarTeam target) {
            this.target = target;
            flag = ((HashMap<String, SerializedLocation>) map().attr().get("flags")).get(target.getTeamName()).toLocation(main.match().getCurrentWorld(), false);
            holder = null;
            captures = 0;
            attempts = 0;
        }

        void addCapture() {
            captures++;
        }

        int getCaptures() {
            return captures;
        }

        void addAttempt() {
            attempts++;
        }

        int getAttempts() {
            return attempts;
        }

        WarTeam getTeam() {
            return target;
        }

        String getHolder() {
            return holder;
        }

        void setHolder(String holder) {
            this.holder = holder;
        }

        Location getFlag() {
            return flag;
        }
    }
}