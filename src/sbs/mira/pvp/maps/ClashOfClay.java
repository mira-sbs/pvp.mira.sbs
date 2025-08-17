package sbs.mira.pvp.maps;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.stored.SerializedLocation;
import sbs.mira.pvp.game.Gamemode;
import sbs.mira.pvp.game.Map;
import sbs.mira.pvp.game.util.SpawnArea;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class ClashOfClay extends Map {

    private final UUID[] creators = {id("d04d579e-78ed-4c60-87d4-39ef95755be6")};
    private final String mapName = "Clash Of Clay";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.TDM, Gamemode.Mode.LP};

    private final WarTeam team1 = new WarTeam("Blue Team", ChatColor.BLUE, 15);
    private final WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 15);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(true, true);
        setBuildBoundary(-51, 0, 4, 172);
        objectives().add(new SpawnArea(main, -25, 7, -22, 10, true, true));
        objectives().add(new SpawnArea(main, -25, 162, -22, 165, true, true));
        setTimeLockTime(4000);
        setMatchDuration(600);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(-23, 81, 164, 180, 0));
        addTeamSpawn(team2, new SerializedLocation(-23, 81, 9, 0, 0));
        setSpectatorSpawn(new SerializedLocation(27.5, 103.5, 86.5, 90, 30));
    }

    @Override
    public void applyInventory(MiraPlayer target) {
        PlayerInventory inv = target.crafter().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.DIAMOND_HELMET, Material.LEATHER_CHESTPLATE});

        inv.setItem(0, new ItemStack(Material.WOOD_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.IRON_PICKAXE));
        inv.setItem(3, new ItemStack(Material.PUMPKIN_PIE, 5));
        inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 2));
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
