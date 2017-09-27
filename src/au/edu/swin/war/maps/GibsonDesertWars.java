package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.game.modes.DTM;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class GibsonDesertWars extends Map {

    private final UUID[] creators = {id("d04d579e-78ed-4c60-87d4-39ef95755be6"),id("5435a69c-d78a-46d7-bba1-02b1c9ed71b9")};
    private final String mapName = "Gibson Desert Wars";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.DTM, Gamemode.Mode.TDM, Gamemode.Mode.LP};

    private final WarTeam team1 = new WarTeam("Blue Team", ChatColor.BLUE, 25);
    private final WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 25);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(true, true);
        setBuildBoundary(-30, -119, 215, 28);
        objectives().add(new DTM.Monument(154, 80, -49, 158, 81, -48, team1, main, false, Material.SMOOTH_BRICK));
        objectives().add(new DTM.Monument(25, 80, -44, 29, 81, -43, team2, main, false, Material.SMOOTH_BRICK));
        setTimeLockTime(18000);
        setMatchDuration(600);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(168.5, 73, -103.5, 315, 0));
        addTeamSpawn(team1, new SerializedLocation(194.5, 61, -101.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(208.5, 75, -88.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(-11.5, 61, 9.5, 180, 0));
        addTeamSpawn(team2, new SerializedLocation(-25.5, 75, -4.5, 225, 0));
        addTeamSpawn(team2, new SerializedLocation(15.5, 73, 11.5, 135, 0));
        setSpectatorSpawn(new SerializedLocation(92, 82, -45.5, 90, 15));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.LEATHER_HELMET, Material.DIAMOND_CHESTPLATE, Material.GOLD_BOOTS});

        inv.setItem(0, new ItemStack(Material.IRON_AXE));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.IRON_PICKAXE));
        inv.setItem(3, new ItemStack(Material.COOKED_FISH, 5, (short) 1));
        inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 2));
        inv.setItem(4, new ItemStack(Material.LOG, 16));
        inv.setItem(27, new ItemStack(Material.ARROW, 16));

        switch (target.getCurrentTeam().getTeamColor()) {
            case RED:
                inv.setItem(5, new ItemStack(Material.STAINED_CLAY, 48, (short) 14));
                break;
            case BLUE:
                inv.setItem(5, new ItemStack(Material.STAINED_CLAY, 48, (short) 11));
                break;
        }
    }
}
