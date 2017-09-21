package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.game.modes.DTM;
import au.edu.swin.war.game.util.SpawnArea;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class Squared extends Map {

    private final UUID[] creators = {};
    private final String mapName = "Squared";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.DTM, Gamemode.Mode.TDM, Gamemode.Mode.LP};

    private final WarTeam team1 = new WarTeam("Green Team", ChatColor.GREEN, 25);
    private final WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 25);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(true, true);
        setBuildBoundary(-103, -51, 12, 12);
        objectives().add(new SpawnArea(main, -118, -34, -105, -5, true, false));
        objectives().add(new SpawnArea(main, 14, -34, 27, -5, true, false));
        objectives().add(new DTM.Monument(-11, 93, -24, -8, 98, -15, team1, Material.STAINED_CLAY, main));
        objectives().add(new DTM.Monument(-83, 93, -24, -80, 98, -15, team2, Material.STAINED_CLAY, main));
        setTimeLockTime(12000);
        setMatchDuration(600);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(23, 93, -14.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(23, 93, -23.5, 0, 0));
        addTeamSpawn(team2, new SerializedLocation(-113, 93, -14.5, 180, 0));
        addTeamSpawn(team2, new SerializedLocation(-113, 93, -23.5, 0, 0));
        setSpectatorSpawn(new SerializedLocation(-45, 103, -68, 0, 15));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.CHAINMAIL_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS});

        inv.setItem(0, new ItemStack(Material.IRON_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.IRON_PICKAXE));
        inv.setItem(3, new ItemStack(Material.GRILLED_PORK, 3));
        inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 2));
        inv.setItem(27, new ItemStack(Material.ARROW, 16));

        switch (target.getCurrentTeam().getTeamColor()) {
            case RED:
                inv.setItem(5, new ItemStack(Material.STAINED_CLAY, 48, (short) 14));
            case GREEN:
                inv.setItem(5, new ItemStack(Material.STAINED_CLAY, 48, (short) 5));
        }
    }
}
