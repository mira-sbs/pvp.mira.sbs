package sbs.mira.pvp.maps;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.stored.SerializedLocation;
import sbs.mira.pvp.game.Gamemode;
import sbs.mira.pvp.game.Map;
import sbs.mira.pvp.game.util.AntiTeamGrief;
import sbs.mira.pvp.game.util.SpawnArea;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class Mutiny extends Map {

    private final UUID[] creators = {id("2e1c067c-6f09-4db0-8cd7-defc12ce622e"), id("a40cdbc0-ce09-4c56-a1a8-7732394b6ad4")};
    private final String mapName = "Mutiny";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.TDM, Gamemode.Mode.LP};

    private final WarTeam team1 = new WarTeam("Captains", ChatColor.BLUE, 30);
    private final WarTeam team2 = new WarTeam("Corsairs", ChatColor.RED, 30);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(true, true);
        setBuildBoundary(-45, -77, 54, 32);
        objectives().add(new SpawnArea(main, -26, 12, -24, 14, false, true));
        objectives().add(new SpawnArea(main, 2, 5, 4, 7, false, true));
        objectives().add(new SpawnArea(main, 31, 11, 33, 13, false, true));
        objectives().add(new SpawnArea(main, -27, -59, -25, -57, false, true));
        objectives().add(new SpawnArea(main, 2, -53, 4, -51, false, true));
        objectives().add(new SpawnArea(main, 30, -60, 32, -58, false, true));
        objectives().add(new AntiTeamGrief(main, team1.getTeamColor(), -45, -77, 54, -41, true));
        objectives().add(new AntiTeamGrief(main, team2.getTeamColor(), -45, -5, 54, 31, true));
        setTimeLockTime(4000);
        setMatchDuration(600);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(-25.5, 94, -57.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(3.5, 85, -51.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(31.5, 91, -58.5, 0, 0));
        addTeamSpawn(team2, new SerializedLocation(32.5, 94, 12.5, 180, 0));
        addTeamSpawn(team2, new SerializedLocation(3.5, 85, 6.5, 180, 0));
        addTeamSpawn(team2, new SerializedLocation(-24.5, 91, 13.5, 180, 0));
        setSpectatorSpawn(new SerializedLocation(-56.5, 86.5, -23.5, 270, 0));
    }

    @Override
    public void applyInventory(MiraPlayer target) {
        PlayerInventory inv = target.crafter().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET});

        inv.setItem(0, new ItemStack(Material.STONE_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.IRON_PICKAXE));
        inv.setItem(3, new ItemStack(Material.COOKED_BEEF, 5));
        inv.setItem(4, new ItemStack(Material.GOLDEN_APPLE, 2));
        inv.setItem(5, new ItemStack(Material.LOG, 6));
        inv.setItem(7, new ItemStack(Material.REDSTONE_TORCH_ON, 2));
        inv.setItem(8, new ItemStack(Material.TNT, 6));
        inv.setItem(27, new ItemStack(Material.ARROW, 48));
    }
}
