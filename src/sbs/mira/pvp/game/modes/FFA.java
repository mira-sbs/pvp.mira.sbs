package sbs.mira.pvp.game.modes;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.game.Gamemode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * an extension to gamemode to implement ffa.
 * created on 2017-04-26.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see MiraPulse
 * @since 1.0.0
 */
public class FFA extends Gamemode {

    private HashMap<UUID, Integer> kills;
    private int leadKills;
    private UUID leader;

    public void reset() {
        kills.clear();
        kills = null;
    }

    public void initialize() {
        kills = new HashMap<>();

        for (WarTeam team : getTeams())
            team.getBukkitTeam().setAllowFriendlyFire(true);

        leadKills = 0;

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
        if (kills.containsKey(killer.crafter().getUniqueId()))
            kills.put(killer.crafter().getUniqueId(), kills.get(killer.crafter().getUniqueId()) + 1);
        else
            kills.put(killer.crafter().getUniqueId(), 1);
        int cKills = kills.get(killer.crafter().getUniqueId());
        if (cKills > leadKills) {
            leadKills = cKills;
            if (!killer.crafter().getUniqueId().equals(leader)) {
                leader = killer.crafter().getUniqueId();
                Bukkit.broadcastMessage(killer.display_name() + " is now the leader");
                logEvent(killer.display_name() + " is now the leader");
            }
        }
        killer.dm("You now have " + cKills + "/" + getFFAKills() + " kills");
        updateScoreboard();
        checkWin(killer.crafter().getUniqueId());
    }

    public void onDeath(MiraPlayer dead) {
    }

    public void decideWinner() {
        int highest = -1;
        ArrayList<String> winners = new ArrayList<>();

        for (Map.Entry<UUID, Integer> entry : kills.entrySet()) {
            MiraPlayer found = main.getWarPlayer(entry.getKey());
            if (found == null) continue;
            int count = entry.getValue();
            if (count == highest)
                winners.add(found.display_name());
            else if (count > highest) {
                highest = count;
                winners.clear();
                winners.add(found.display_name());
            }
        }

        if (winners.size() > 1) {
            Bukkit.broadcastMessage("It's a " + winners.size() + "-way tie! " + main.strings().sentenceFormat(winners) + " tied!");
            tempWinner = main.strings().sentenceFormat(winners);
        } else if (winners.size() == 1) {
            String winner = winners.get(0);
            Bukkit.broadcastMessage(winner + ChatColor.WHITE + " is the winner with " + highest + " points!");
            tempWinner = main.strings().sentenceFormat(winners);
        }
    }

    public String getOffensive() {
        return "Kill players to score points for yourself!";
    }

    public String getDefensive() {
        return "Don't let other players kill you!";
    }

    public String getFullName() {
        return "Free For All";
    }

    public String getName() {
        return "FFA";
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

        obj.getScore(" ").setScore(3);
        obj.getScore("  Leader's Kills").setScore(2);
        obj.getScore("    " + leadKills + "/" + getFFAKills()).setScore(1);
        obj.getScore("  ").setScore(0);
        s().resetScores("    " + (leadKills - 1) + "/" + getFFAKills());

    }

    /**
     * if the player reaches the kill cap, this
     * procedure will automatically end the round.
     *
     * @param player player to check.
     */
    private void checkWin(UUID player) {
        if (kills.get(player) >= getFFAKills())
            onEnd();
    }

    /**
     * returns the map's defined score cap for FFA.
     * ny default, this score cap is set to 20.
     *
     * @return FFA score cap.
     */
    private Integer getFFAKills() {
        return (Integer) map().attr().get("ffaKills");
    }

    @Override
    public HashMap<String, Object> getExtraTeamData(WarTeam team) {
        return new HashMap<>();
    }
}
