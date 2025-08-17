package sbs.mira.pvp.game.modes;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * an extension to gamemode to implement lp.
 * created on 2017-04-21.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see MiraPulse
 * @since 1.0.0
 */
public class LP extends Gamemode {

    private final HashMap<String, Integer> lives = new HashMap<>();

    public void reset() {
        lives.clear();
    }

    public void initialize() {
        for (WarTeam team : getTeams())
            lives.put(team.getTeamName(), (Bukkit.getOnlinePlayers().size() * 5) + 3);

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
        death(killed);
    }

    public void onDeath(MiraPlayer killed) {
        death(killed);
    }

    /**
     * procedure that handles death within a round.
     * a life is decremented from the dead player's
     * team's life pool.
     *
     * @param killed player who died.
     */
    private void death(MiraPlayer killed) {
        int lives = this.lives.get(killed.getCurrentTeam().getTeamName());
        if (lives == 0) return;
        this.lives.put(killed.getCurrentTeam().getTeamName(), lives - 1);
        updateScoreboard();
        checkWin();
    }

    public void decideWinner() {
        int highest = -1;
        ArrayList<WarTeam> winners = new ArrayList<>();

        for (WarTeam team : getTeams()) {
            int count = lives.get(team.getTeamName());
            if (count == highest)
                winners.add(team);
            else if (count > highest) {
                highest = count;
                winners.clear();
                winners.add(team);
            }
        }
        broadcastWinner(winners, "lives remaining", highest);
    }

    private void checkWin() {
        int aliveTeams = 0;
        for (WarTeam team : getTeams())
            if (lives.get(team.getTeamName()) >= 1)
                aliveTeams++;
        if (aliveTeams <= 1)
            onEnd();
    }

    public String getOffensive() {
        return "Kill enemies to deplete their lifepool!";
    }

    public String getDefensive() {
        return "Protect your team and your lives!";
    }

    public String getName() {
        return "LP";
    }

    public String getFullName() {
        return "Lifepool";
    }

    public String getGrammar() {
        return "an";
    }

    public void onLeave(MiraPlayer left) {
    }

    public void updateScoreboard() {
        Objective obj = s().getObjective(DisplaySlot.SIDEBAR);

        String dp = map().getMapName() + " (" + getName() + ")";
        if (dp.length() > 32) dp = dp.substring(0, 32);
        obj.setDisplayName(dp);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(" ").setScore(lives.size() + 2);
        obj.getScore("  Lives Remaining").setScore(lives.size() + 1);

        Iterator<WarTeam> iterator = getTeams().iterator();
        for (int i = 0; i < lives.size(); i++) {
            WarTeam target = iterator.next();
            obj.getScore(target.getTeamColor() + "    " + lives.get(target.getTeamName())).setScore(i + 1);
            s().resetScores(target.getTeamColor() + "    " + (lives.get(target.getTeamName()) + 1));
        }
        obj.getScore("  ").setScore(0);
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Lives Remaining", lives.get(team.getTeamName()));
        return extra;
    }
}
