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
import java.util.UUID;

/**
 * an extension to gamemode to implement lms.
 * created on 2017-04-26.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see MiraPulse
 * @since 1.0.0
 */
public class LMS extends Gamemode {

    private ArrayList<UUID> participated;
    private ArrayList<UUID> alive;

    public void reset() {
        if (participated != null)
            while (participated.size() > 0) {
                MiraPlayer wp = main.getWarPlayer(participated.get(0));
                if (wp != null)
                    wp.setJoined(true);
                participated.remove(participated.get(0));
            }
        participated = null;
        
        if (alive != null)
            alive.clear();
        alive = null;
    }

    public void initialize() {
        alive = new ArrayList<>();
        participated = new ArrayList<>();

        if (getJoined() < 2) {
            // LMS requires 2 players at the least to play.
            Bukkit.broadcastMessage("There needs to be 2 or more participating players!");
            logEvent("Match cancelled as there was not enough players");
            onEnd();
            return;
        }

        for (WarTeam team : getTeams())
            team.getBukkitTeam().setAllowFriendlyFire(true);

        autoAssign();

        for (MiraPlayer check : main.getWarPlayers().values())
            if (check.is_member_of_team()) {
                alive.add(check.crafter().getUniqueId());
                participated.add(check.crafter().getUniqueId());
            }

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
        alive.remove(dead.crafter().getUniqueId());

        updateScoreboard();

        dead.setJoined(false);
        entryHandle(dead);

        checkWin();
    }

    public void decideWinner() {
        if (alive.size() == 1) {
            MiraPlayer winner = main.getWarPlayer(alive.get(0));
            if (winner != null) {
                tempWinner = winner.display_name();
                Bukkit.broadcastMessage(winner.display_name() + " is the last man standing!");
                return;
            }
        }
        Bukkit.broadcastMessage("There was no winner this match!");
        tempWinner = "No one";
    }

    public String getOffensive() {
        return "Kill other players!";
    }

    public String getDefensive() {
        return "Don't get yourself killed!";
    }

    public String getFullName() {
        return "Last Man Standing";
    }

    public String getName() {
        return "LMS";
    }

    public String getGrammar() {
        return "an";
    }

    public void onLeave(MiraPlayer left) {
        if (!alive.contains(left.crafter().getUniqueId())) return;

        alive.remove(left.crafter().getUniqueId());

        updateScoreboard();
        checkWin();
    }

    public void updateScoreboard() {
        Objective obj = s().getObjective(DisplaySlot.SIDEBAR);

        String dp = map().getMapName() + " (" + getName() + ")";
        if (dp.length() > 32) dp = dp.substring(0, 32);
        obj.setDisplayName(dp);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        obj.getScore(" ").setScore(3);
        obj.getScore("  Still Standing").setScore(2);
        obj.getScore("    " + alive.size() + "/" + participated.size()).setScore(1);
        obj.getScore("  ").setScore(0);
        s().resetScores("    " + (alive.size() + 1) + "/" + participated.size());

    }

    /**
     * if there is 1 or less players remaining,
     * the match is over since it is a last man
     * standing match.
     */
    private void checkWin() {
        if (alive.size() <= 1 && active)
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
        extra.put("Participants", participated.size());
        return extra;
    }
}
