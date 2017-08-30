package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class Xenon extends Map {

    private final UUID[] creators = {UUID.fromString("df5fd9f4-4840-4293-9346-5c77bf7bc08f")};
    private final String mapName = "Xenon";
    private final Material[] disabledDrops = defaultDisabledDrops();
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.FFA, Gamemode.Mode.LMS};

    private final WarTeam team1 = new WarTeam("Green Team", ChatColor.GREEN, 25);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setDisabledDrops(disabledDrops);
        setGamemodes(gamemodes);
        registerTeam(team1);
        setAllowBuild(false, false);
        setTimeLockTime(18000);
        setMatchDuration(300);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(63.5, 6, -16.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(64.5, 6, 27.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(43.5, 6, 26.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(42.5, 6, -17.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(53.5, 3, -37.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(53.5, 3, -17.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(53.5, 3, -5.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(53.5, 3, 47.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(53.5, 3, 27.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(53.5, 3, 16.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(67.5, 2, -30.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(39.5, 2, -30.5, 0, 0));
        addTeamSpawn(team1, new SerializedLocation(39.5, 2, 40.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(67.5, 2, 40.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(39.5, 6, 19.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(35.5, 2, 23.5, 225, 0));
        addTeamSpawn(team1, new SerializedLocation(67.5, 6, -9.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(71.5, 2, -13.5, 45, 0));
        addTeamSpawn(team1, new SerializedLocation(21.5, 2, 2.5, 270, 0));
        addTeamSpawn(team1, new SerializedLocation(42.5, 2, 2.5, 270, 0));
        addTeamSpawn(team1, new SerializedLocation(85.5, 2, 5.5, 90, 0));
        addTeamSpawn(team1, new SerializedLocation(64.5, 2, 5.5, 90, 0));
        addTeamSpawn(team1, new SerializedLocation(75.5, 10, 11.5, 90, 0));
        addTeamSpawn(team1, new SerializedLocation(75.5, 10, -1.5, 90, 0));
        addTeamSpawn(team1, new SerializedLocation(31.5, 10, -1.5, 270, 0));
        addTeamSpawn(team1, new SerializedLocation(31.5, 10, 11.5, 270, 0));
        addTeamSpawn(team1, new SerializedLocation(47.5, 2, 22.5, 270, 0));
        addTeamSpawn(team1, new SerializedLocation(59.5, 2, -12.5, 90, 0));
        addTeamSpawn(team1, new SerializedLocation(71.5, 2, 23.5, 135, 0));
        addTeamSpawn(team1, new SerializedLocation(35.5, 2, -13.5, 315, 0));
        setSpectatorSpawn(new SerializedLocation(53.5, 3, 40.5, 180, 0));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        main.items().applyColoredArmorAccordingToTeam(target, new Material[]{Material.LEATHER_BOOTS, Material.LEATHER_CHESTPLATE});
        inv.setHelmet(new ItemStack(Material.IRON_HELMET));
        inv.setLeggings(new ItemStack(Material.IRON_LEGGINGS));

        inv.setItem(0, new ItemStack(Material.STONE_SWORD, 1, (short) -16373));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.COOKED_BEEF, 2));
        inv.setItem(3, new ItemStack(Material.EXP_BOTTLE, 5));
        inv.setItem(27, new ItemStack(Material.ARROW, 16));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to.getBlockX() < 15 && to.getBlockZ() < 8 && to.getBlockZ() > 2) {
            event.getPlayer().teleport(new Location(to.getWorld(), 91, 3.5, 5.5, 90, 0));
            event.getPlayer().setVelocity(new Vector(-0.25, 0, 0));
        } else if (to.getBlockX() > 91 && to.getBlockZ() < 8 && to.getBlockZ() > 2) {
            event.getPlayer().teleport(new Location(to.getWorld(), 16, 3.5, 5.5, 270, 0));
            event.getPlayer().setVelocity(new Vector(0.25, 0, 0));
        }
    }
}
