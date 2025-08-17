package sbs.mira.pvp.game.modes;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.stored.Activatable;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

/**
 * an extension to gamemode to implement ddm.
 * created on 2017-04-26.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see MiraPulse
 * @since 1.0.0
 */
public class DDM extends Gamemode {

    private HashMap<String, Integer> scores;

    public void reset() {
        if (scores != null)
            scores.clear();
        scores = null;
    }

    public void initialize() {
        scores = new HashMap<>();

        for (WarTeam team : getTeams())
            scores.put(team.getTeamName(), Bukkit.getOnlinePlayers().size() * 3);
        autoAssign();
        
        Objective obj = s().registerNewObjective("gm", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateScoreboard();

        for (Player online : Bukkit.getOnlinePlayers())
            online.setScoreboard(s());
    }

    public void tick() {
    }

    public void onKill(MiraPlayer killed, MiraPlayer killer) {
    }

    public void onDeath(MiraPlayer killed) {
    }

    public void onLeave(MiraPlayer left) {
    }

    public String getOffensive() {
        return "Run into the enemy's \"territory\" to score a lot of points!";
    }

    public String getDefensive() {
        return "Stop the enemy from getting into your \"territory\"!";
    }

    public String getFullName() {
        return "District Death Match";
    }

    public String getName() {
        return "DDM";
    }

    public String getGrammar() {
        return "a";
    }

    public void decideWinner() {
        int lowest = 999;
        ArrayList<WarTeam> winners = new ArrayList<>(); // Keep a temporary list of winners.

        for (WarTeam team : getTeams()) {
            int count = scores.get(team.getTeamName());
            if (count == lowest)
                winners.add(team);
            else if (count < lowest) {
                lowest = count;
                winners.clear();
                winners.add(team);
            }
        }
        broadcastWinner(winners, "run-ins remaining", lowest);
    }

    public void updateScoreboard() {
        Objective obj = s().getObjective(DisplaySlot.SIDEBAR);

        String dp = map().getMapName() + " (" + getName() + ")";
        if (dp.length() > 32) dp = dp.substring(0, 32);
        obj.setDisplayName(dp);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(" ").setScore(scores.size() + 2);
        obj.getScore("  Run-ins Remaining").setScore(scores.size() + 1);

        Iterator<WarTeam> iterator = getTeams().iterator();
        for (int i = 0; i < getTeams().size(); i++) {
            WarTeam target = iterator.next();
            obj.getScore(target.getTeamColor() + "    " + scores.get(target.getTeamName())).setScore(i + 1);
            s().resetScores(target.getTeamColor() + "    " + (scores.get(target.getTeamName()) + 1));
        }
        obj.getScore("  ").setScore(0);
    }

    @Override
    protected HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Remaining Score", scores.get(team.getTeamName()));
        return extra;
    }

    /**
     * a territory is a cuboid, in which if an
     * opposing player runs into, scores a lot
     * of points for their team on DDM. Alongside
     * killing enemy players, they must also
     * protect their territory from being entered.
     */
    public static class Territory implements Listener, Activatable {
        final int x1;
        final int y1;
        final int z1;
        final int x2;
        final int y2;
        final int z2;
        final String belongsTo;
        final MiraPulse main;

        public Territory(int x1, int y1, int z1, int x2, int y2, int z2, WarTeam belongsTo, MiraPulse main) {
            this.x1 = Math.min(x1, x2);
            this.y1 = Math.min(y1, y2);
            this.z1 = Math.min(z1, z2);
            this.x2 = Math.max(x1, x2);
            this.y2 = Math.max(y1, y2);
            this.z2 = Math.max(z1, z2);
            this.belongsTo = belongsTo.getDisplayName();
            this.main = main;
        }

        /**
         * awaken this territory cuboid for the match.
         */
        public void activate() {
            if (!main.match().getCurrentMode().getFullName().equals("District Death Match"))
                return;

            main.plugin().getServer().getPluginManager().registerEvents(this, main.plugin());
        }

        /**
         * put this territory cuboid to sleep until it is needed again.
         */
        public void deactivate() {
            HandlerList.unregisterAll(this);
        }

        /**
         * checks if a location is inside the cuboid.
         * this is used to check if a player has entered
         * this territory and needs to be acted upon.
         *
         * @param loc the location to compare.
         * @return are they inside the territory?
         */
        boolean isInside(Location loc) {
            return loc.getBlockX() >= x1 && loc.getBlockX() <= x2 && loc.getBlockY() >= y1 && loc.getBlockY() <= y2 && loc.getBlockZ() >= z1 && loc.getBlockZ() <= z2;
        }

        @EventHandler
        public void nmv(PlayerMoveEvent event) {
            if (isInside(event.getTo()) && !event.getPlayer().isDead()) {
                if (!event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)) return;
                MiraPlayer wp = main.getWarPlayer(event.getPlayer());
                if (wp.getCurrentTeam() == null) return;

                WarTeam target = wp.getCurrentTeam();
                if (!target.getDisplayName().equals(belongsTo)) {
                    DDM ddm = (DDM) main.cache().getGamemode("District Death Match");
                    for (MiraPlayer wp2 : main.getWarPlayers().values()) {
                        if (wp2.getCurrentTeam() == null) continue;
                        if (!wp2.getCurrentTeam().getTeamName().equals(target.getTeamName()))
                            wp2.crafter().playSound(wp2.crafter().getLocation(), Sound.ENTITY_GHAST_SCREAM, 1F, 1F);
                        else
                            wp2.crafter().playSound(wp2.crafter().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
                    }

                    Bukkit.broadcastMessage(wp.display_name() + " scored a point");
                    ddm.logEvent(wp.display_name() + " scored a point");

                    int capsToGo = ddm.scores.get(target.getTeamName());
                    ddm.scores.put(target.getTeamName(), capsToGo - 1);

                    ddm.updateScoreboard();
                    if (capsToGo == 1)
                        ddm.onEnd();
                    else
                        event.setTo(ddm.map().getTeamSpawns(target.getTeamName()).get(new Random().nextInt(ddm
                                .map().getTeamSpawns(target.getTeamName()).size())).toLocation(main.match().getCurrentWorld(), true));
                } else {
                    main.warn(event.getPlayer(), "You're supposed to stop the enemy from getting into here!");
                    event.setTo(event.getFrom());
                }
            }
        }

    }
}