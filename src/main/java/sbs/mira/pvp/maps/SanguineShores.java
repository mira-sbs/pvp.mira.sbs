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
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class SanguineShores extends Map {

    private final UUID[] creators = {id("2e1c067c-6f09-4db0-8cd7-defc12ce622e"), id("a40cdbc0-ce09-4c56-a1a8-7732394b6ad4")};
    private final String mapName = "Sanguine Shores";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.TDM, Gamemode.Mode.KOTH};

    private final WarTeam team1 = new WarTeam("Tourists", ChatColor.DARK_PURPLE, 20);
    private final WarTeam team2 = new WarTeam("Locals", ChatColor.AQUA, 20);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(false, false);
        attr().put("kothFlag", new SerializedLocation(0,93,0));
        objectives().add(new SpawnArea(main, -69, 2, -67, 4, true, true));
        objectives().add(new SpawnArea(main, 67, -4, 69, -2, true, true));
        setTimeLockTime(5000);
        setMatchDuration(600);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(68.5, 93, -2.5, 90, 0));
        addTeamSpawn(team2, new SerializedLocation(-67.5, 93, 3.5, 270, 0));
        setSpectatorSpawn(new SerializedLocation(15.5, 82, 26.5, 225, 0));
    }

    @Override
    public void applyInventory(MiraPlayer target) {
        PlayerInventory inv = target.crafter().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.CHAINMAIL_HELMET, Material.LEATHER_CHESTPLATE, Material.IRON_LEGGINGS, Material.CHAINMAIL_BOOTS});

        inv.setItem(0, new ItemStack(Material.IRON_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.PUMPKIN_PIE, 6));
        inv.setItem(3, main.items().createPotion(PotionEffectType.HEAL, 0, 1, 1));
        inv.setItem(9, new ItemStack(Material.ARROW, 32));
    }
}
