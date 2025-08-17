package sbs.mira.pvp.game.modes;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

/**
 * an extension to gamemode to implement lts.
 * created on 2017-04-26.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @since 1.0.0
 */
public class LTS extends Gamemode {

    private ArrayList<UUID> participated;
    private HashMap<String, Integer> original;

    public void reset() {
        if (participated != null)
            while (participated.size() > 0) {
                MiraPlayer wp = main.getWarPlayer(participated.get(0));
                if (wp != null)
                    wp.setJoined(true);
                participated.remove(participated.get(0));
            }
        participated = null;
        original.clear();
        original = null;
    }

    public void initialize() {
        participated = new ArrayList<>();
        original = new HashMap<>();

        if (getJoined() < 2) {
            Bukkit.broadcastMessage("There needs to be 2 or more participating players!");
            logEvent("Match cancelled as there was not enough players");
            onEnd();
            return;
        }

        autoAssign();

        for (MiraPlayer check : main.getWarPlayers().values())
            if (check.is_member_of_team())
                participated.add(check.crafter().getUniqueId());

        for (WarTeam team : getTeams())
            original.put(team.getTeamName(), team.getBukkitTeam().getEntries().size());

        permaDeath = true;

        Objective obj = s().registerNewObjective("gm", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        updateScoreboard();

        for (Player online : Bukkit.getOnlinePlayers())
            online.setScoreboard(s());
    }

    public void tick() {
    }

    public void onKill(MiraPlayer killed, MiraPlayer killer) {
        dead(killed);
    }

    public void onDeath(MiraPlayer dead) {
        dead(dead);
    }

    /**
     * common code is shared by onKill and onDeath,
     * both call to this procedure to prevent duplication.
     *
     * @param dead the player who died.
     */
    private void dead(MiraPlayer dead) {
        dead.setJoined(false);
        entryHandle(dead);
    }

    public void decideWinner() {
        int highest = 0;
        ArrayList<WarTeam> winners = new ArrayList<>();

        for (WarTeam team : getTeams()) {
            int count = team.getBukkitTeam().getSize();
            if (count == highest)
                winners.add(team);
            else if (count > highest) {
                highest = count;
                winners.clear();
                winners.add(team);
            }
        }
        broadcastWinner(winners, "members remaining", highest);
    }

    public String getOffensive() {
        return "Kill other players!";
    }

    public String getDefensive() {
        return "Don't get yourself killed!";
    }

    public String getFullName() {
        return "Last Team Standing";
    }

    public String getName() {
        return "LTS";
    }

    public String getGrammar() {
        return "an";
    }

    public void onLeave(MiraPlayer left) {
        updateScoreboard();
        checkWin();
    }

    public void updateScoreboard() {
        Objective obj = s().getObjective(DisplaySlot.SIDEBAR);

        String dp = map().getMapName() + " (" + getName() + ")";
        if (dp.length() > 32) dp = dp.substring(0, 32);
        obj.setDisplayName(dp);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(" ").setScore(getTeams().size() + 2);
        obj.getScore("  Players Remaining").setScore(getTeams().size() + 1);

        Iterator<WarTeam> iterator = getTeams().iterator();
        for (int i = 0; i < getTeams().size(); i++) {
            WarTeam target = iterator.next();
            obj.getScore(target.getTeamColor() + "    " + target.getBukkitTeam().getEntries().size()).setScore(i + 1);
            s().resetScores(target.getTeamColor() + "    " + (target.getBukkitTeam().getEntries().size() + 1));
        }
        obj.getScore("  ").setScore(0);
    }

    /**
     * check if there is 1 or less teams with 1
     * or more players remaining. if that is the
     * case, end the round.
     */
    private void checkWin() {
        if (!active) return;
        int remainingTeams = 0;
        for (WarTeam team : getTeams())
            if (team.getBukkitTeam().getEntries().size() >= 1) remainingTeams++;

        if (remainingTeams <= 1)
            onEnd();

    }

    /**
     * sneaking is a strategy often used to hide
     * on maps, so sneaking will not allow you to
     * hide your name tag behind walls.
     *
     * @param event an event called by Spigot.
     */
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        event.setCancelled(true);
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        HashMap<String, Object> extra = new HashMap<>();
        extra.put("Participants", original.get(team.getTeamName()));
        return extra;
    }
}
