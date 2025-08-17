package sbs.mira.pvp.maps;

import sbs.mira.pvp.framework.MiraPlayer;
import sbs.mira.pvp.framework.game.WarTeam;
import sbs.mira.pvp.framework.stored.SerializedLocation;
import sbs.mira.pvp.game.Gamemode;
import sbs.mira.pvp.game.Map;
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

    private final UUID[] creators = {id("2e1c067c-6f09-4db0-8cd7-defc12ce622e")};
    private final String mapName = "Fairwick Village";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.CTF};

    private final WarTeam team1 = new WarTeam("Blue Team", ChatColor.BLUE, 20);
    private final WarTeam team2 = new WarTeam("Red Team", ChatColor.RED, 20);

    private final ItemStack GADGET = createGadget(Material.FIREWORK, 3, 0, "Jumpwork", "Right click on a block to rise up", "Jump at the right time for max height");

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(true, false);
        setTimeLockTime(2000);
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
    public void applyInventory(MiraPlayer target) {
        PlayerInventory inv = target.crafter().getInventory();

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
        if (!isAction(event, Action.RIGHT_CLICK_BLOCK)) return;
        if (!useGadget(event, event.getHand(), GADGET, false)) return;
        Player pl = event.getPlayer();
        pl.setVelocity(new Vector(pl.getVelocity().getX(), pl.getVelocity().getY() < 0 ? 0.55 : 1.05, pl.getVelocity().getZ()));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.STAINED_GLASS_PANE) event.setCancelled(true);
    }
}
