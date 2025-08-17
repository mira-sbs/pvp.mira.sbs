package sbs.mira.pvp.maps;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.stored.SerializedLocation;
import sbs.mira.pvp.game.Gamemode;
import sbs.mira.pvp.game.Map;
import sbs.mira.pvp.game.util.RadialSpawnPoint;
import sbs.mira.pvp.game.util.SpawnArea;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class SanguineShoresII extends Map {

    private final UUID[] creators = {id("2e1c067c-6f09-4db0-8cd7-defc12ce622e"), id("a40cdbc0-ce09-4c56-a1a8-7732394b6ad4")};
    private final String mapName = "Sanguine Shores II";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.CTF, Gamemode.Mode.LP};

    private final WarTeam team1 = new WarTeam("Red Team", ChatColor.RED, 25);
    private final WarTeam team2 = new WarTeam("Blue Team", ChatColor.BLUE, 25);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(true, true);
        setPlateauY(35);
        objectives().add(new SpawnArea(main, 86, -7, 100, 7, true, true));
        objectives().add(new SpawnArea(main, -53, -29, -39, -15, true, true));
        attr().put("captureRequirement", 2);
        addCTFFlag(team1.getTeamName(), new SerializedLocation(70, 59, -18));
        addCTFFlag(team2.getTeamName(), new SerializedLocation(-23, 59, -4));
        setTimeLockTime(6000);
        setMatchDuration(600);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new RadialSpawnPoint(main.rng, 93.5, 57, 0.5, 90, 0, 4, 4));
        addTeamSpawn(team2, new RadialSpawnPoint(main.rng, -46.5, 57, -22.5, 270, 0, 4, 4));
        setSpectatorSpawn(new RadialSpawnPoint(main.rng, 24, 55, -53.5, 0, 0, 2, 1));
    }

    @Override
    public void applyInventory(MiraPlayer target) {
        PlayerInventory inv = target.crafter().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS});

        inv.setItem(0, new ItemStack(Material.IRON_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack (Material.STONE_AXE));
        inv.setItem(3, new ItemStack(Material.COOKED_FISH, 6, (short) 1));
        inv.setItem(9, new ItemStack(Material.ARROW, 32));

        switch(target.getCurrentTeam().getTeamColor()){
            case RED:
                inv.setItem(4,new ItemStack(Material.CONCRETE_POWDER,5,(short)14));
                break;
            case BLUE:
                inv.setItem(4, new ItemStack(Material.CONCRETE_POWDER, 5, (short)11));
                break;
        }
    }
}
