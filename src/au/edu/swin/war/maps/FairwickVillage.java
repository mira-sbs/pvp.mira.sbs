package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.UUID;

@SuppressWarnings("Duplicates")
public class FairwickVillage extends Map {

    private final UUID[] creators = {UUID.fromString("2cfb556d-55f9-4fa3-8043-199a15d11f40")};
    private final String mapName = "Fairwick Village";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.CTF};

    private final WarTeam team1 = new WarTeam("Blue Team", ChatColor.BLUE, 20);
    private final WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 20);

    private final ItemStack GADGET = createGadget(Material.FIREWORK, 3, 0, "Jumpwork", "Right click to fly up");

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(true, false);
        setTimeLockTime(14000);
        setMatchDuration(600);

        addCTFFlag(team1.getTeamName(), new SerializedLocation(72, 74, 136));
        addCTFFlag(team2.getTeamName(), new SerializedLocation(72, 74, -2));
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(36.5, 73, 116.5, 180, 0));
        addTeamSpawn(team1, new SerializedLocation(108.5, 73, 116.5, 180, 0));
        addTeamSpawn(team2, new SerializedLocation(108.5, 73, 18.5, 0, 0));
        addTeamSpawn(team2, new SerializedLocation(36.5, 73, 18.5, 0, 0));
        setSpectatorSpawn(new SerializedLocation(72.5, 78, 67.5, 90, 0));
    }

    @Override
    public void applyInventory(WarPlayer target) {
        PlayerInventory inv = target.getPlayer().getInventory();

        main.items().applyArmorAcccordingToTeam(target, new Material[]{Material.IRON_HELMET, Material.LEATHER_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS});

        inv.setItem(0, new ItemStack(Material.IRON_SWORD));
        inv.setItem(1, new ItemStack(Material.BOW));
        inv.setItem(2, new ItemStack(Material.COOKED_BEEF, 5));
        inv.setItem(3, new ItemStack(Material.GOLDEN_APPLE, 2));
        inv.setItem(8, GADGET);
        inv.setItem(27, new ItemStack(Material.ARROW, 16));

    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (!isAction(event, Action.RIGHT_CLICK_AIR, Action.RIGHT_CLICK_BLOCK)) return;
        if (!useGadget(event.getPlayer().getInventory(), GADGET)) return;
        Player pl = event.getPlayer();
        pl.setVelocity(pl.getVelocity().add(new Vector(0, 1.05, 0)));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.STAINED_GLASS_PANE) event.setCancelled(true);
    }
}
