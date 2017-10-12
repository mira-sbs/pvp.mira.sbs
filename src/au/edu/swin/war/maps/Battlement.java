package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.game.util.RadialSpawnPoint;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class Battlement extends Map {

    private final UUID[] creators = {id("9b733374-2418-4c5a-b6a4-d27b77020903")};
    private final String mapName = "Battlement";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.CTF, Gamemode.Mode.LTS, Gamemode.Mode.LP};

    private final WarTeam team1 = new WarTeam("Blue Team", ChatColor.BLUE, 20);
    private final WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 20);


    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(false, false);
        addCTFFlag(team1.getTeamName(), new SerializedLocation(-31, 84, 20));
        addCTFFlag(team2.getTeamName(), new SerializedLocation(27, 84, -29));
        attr().put("captureRequirement", 2);
        setTimeLockTime(6000);
        setMatchDuration(900);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(-15.5, 70, 4.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(-23.5, 70, 4.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(-31.5, 70, 4.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(-39.5, 70, 4.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(-15.5, 70, 12.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(-15.5, 70, 20.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(-15.5, 70, 28.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(-23.5, 74, 12.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(-31.5, 74, 12.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(-23.5, 74, 20.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(-27.5, 78, 16.5, 225, 0));
        addTeamSpawn(team2, new SerializedLocation(11.5, 70, -13.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(19.5, 70, -13.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(27.5, 70, -13.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(35.5, 70, -13.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(11.5, 70, -21.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(11.5, 70, -29.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(11.5, 70, -37.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(19.5, 74, -21.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(27.5, 74, -21.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(19.5, 74, -29.5, 45, 0));
        addTeamSpawn(team2, new SerializedLocation(23.5, 78, -25.5, 45, 0));
        setSpectatorSpawn(new RadialSpawnPoint(main.rng, 13.5, 93, 17.5, 135, 25, 3, 3));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.IRON_HELMET, Material.LEATHER_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS});

        inv.setItem(0, new ItemStack(Material.IRON_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.COOKED_BEEF, 16));
        inv.setItem(3, main.items().createPotion(PotionEffectType.HEAL, 0, 1, 1));
        inv.setItem(4, new ItemStack(Material.EXP_BOTTLE, 2));
        inv.setItem(27, new ItemStack(Material.ARROW, 28));

        target.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 40, 4));
    }
}
