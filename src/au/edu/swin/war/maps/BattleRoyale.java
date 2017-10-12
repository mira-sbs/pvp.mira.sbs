package au.edu.swin.war.maps;

import au.edu.swin.war.framework.WarPlayer;
import au.edu.swin.war.framework.game.WarTeam;
import au.edu.swin.war.framework.stored.SerializedLocation;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.game.util.SpawnArea;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityTNTPrimed;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftTNTPrimed;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.UUID;

@SuppressWarnings("Duplicates")
public class BattleRoyale extends Map {

    private final UUID[] creators = {id("2e1c067c-6f09-4db0-8cd7-defc12ce622e"), id("a40cdbc0-ce09-4c56-a1a8-7732394b6ad4")};
    private final String mapName = "Battle Royale";
    private final Gamemode.Mode[] gamemodes = {Gamemode.Mode.TDM, Gamemode.Mode.LP};

    private final WarTeam team1 = new WarTeam("Purple Team", ChatColor.DARK_PURPLE, 20);
    private final WarTeam team2 = new WarTeam("Cyan Team", ChatColor.DARK_AQUA, 20);

    protected void readyAttributes() {
        setMapName(mapName);
        setCreators(creators);
        setGamemodes(gamemodes);
        setDisabledDrops(Material.values());
        registerTeam(team1);
        registerTeam(team2);
        setAllowBuild(false, false);
        objectives().add(new SpawnArea(main, -37, 19, -24, 30, false, true));
        objectives().add(new SpawnArea(main, -37, -30, -24, -19, false, true));
        setTimeLockTime(14000);
        setMatchDuration(600);
    }

    protected void readySpawns() {
        addTeamSpawn(team1, new SerializedLocation(-33.5, 85, 25.5, 255, 0));
        addTeamSpawn(team1, new SerializedLocation(-28.5, 85, 25.5, 135, 0));
        addTeamSpawn(team2, new SerializedLocation(-33.5, 85, -25.5, 315, 0));
        addTeamSpawn(team2, new SerializedLocation(-28.5, 85, -25.5, 45, 0));
        setSpectatorSpawn(new SerializedLocation(-2.5, 84, 0.5, 90, 0));
    }

    @Override
    public void applyInventory(WarPlayer target) {
    }

    private interface Apply {
        void apply(WarPlayer pl);
    }

    private enum Class {
        Hero(
                pl -> {
                    PlayerInventory inv = pl.getPlayer().getInventory();
                    inv.setItem(0, new ItemStack(Material.IRON_SWORD));
                    inv.setItem(1, new ItemStack(Material.COOKED_BEEF, 16));
                },
                new Material[]{Material.IRON_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.CHAINMAIL_BOOTS}
        ),
        Tank(
                pl -> {
                    PlayerInventory inv = pl.getPlayer().getInventory();
                    inv.setItem(0, new ItemStack(Material.WOOD_SWORD));
                    inv.setItem(1, new ItemStack(Material.COOKED_BEEF, 16));
                    inv.setItem(8, new ItemStack(Material.SHIELD));
                    pl.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 600, 0));
                },
                new Material[]{Material.IRON_HELMET, Material.DIAMOND_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.IRON_BOOTS}
        ),
        Scout(
                pl -> {
                    PlayerInventory inv = pl.getPlayer().getInventory();
                    inv.setItem(0, new ItemStack(Material.STONE_SWORD));
                    inv.setItem(1, new ItemStack(Material.COOKED_BEEF, 16));
                    inv.setItem(2, new ItemStack(Material.GOLDEN_APPLE, 2));
                    pl.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 600, 1));
                    pl.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 600, 1));
                },
                new Material[]{Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_LEGGINGS}
        ),
        Assassin(
                pl -> {
                    PlayerInventory inv = pl.getPlayer().getInventory();
                    ItemStack SWORD = new ItemStack(Material.IRON_SWORD);
                    SWORD.addEnchantment(Enchantment.DAMAGE_ALL, 4);
                    inv.setItem(0, SWORD);
                    inv.setItem(1, new ItemStack(Material.COOKED_BEEF, 16));
                    pl.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 600, 0));
                },
                new Material[0]
        ),
        Medic(
                pl -> {
                    PlayerInventory inv = pl.getPlayer().getInventory();
                    inv.setItem(0, new ItemStack(Material.GOLD_SWORD));
                    inv.setItem(1, new ItemStack(Material.COOKED_BEEF, 16));

                    ItemStack HEALING_POTION = new ItemStack(Material.SPLASH_POTION, 48);
                    PotionMeta meta = (PotionMeta) HEALING_POTION.getItemMeta();
                    meta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 0, 1), true);
                    meta.setColor(PotionEffectType.HEAL.getColor());
                    meta.setDisplayName("Splash Potion of Healing");
                    HEALING_POTION.setItemMeta(meta);

                    ItemStack DAMAGE_POTION = new ItemStack(Material.SPLASH_POTION, 6);
                    meta = (PotionMeta) DAMAGE_POTION.getItemMeta();
                    meta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 0, 1), true);
                    meta.setColor(PotionEffectType.HARM.getColor());
                    meta.setDisplayName("Splash Potion of Harming");
                    DAMAGE_POTION.setItemMeta(meta);

                    inv.setItem(2, HEALING_POTION);
                    inv.setItem(3, DAMAGE_POTION);
                    inv.setItem(8, new ItemStack(Material.TOTEM));
                },
                new Material[]{Material.GOLD_HELMET, Material.LEATHER_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS}
        ),
        Sniper(
                pl -> {
                    PlayerInventory inv = pl.getPlayer().getInventory();
                    inv.setItem(0, new ItemStack(Material.STONE_AXE));

                    ItemStack BOW = new ItemStack(Material.BOW);
                    BOW.addEnchantment(Enchantment.ARROW_INFINITE, 1);

                    inv.setItem(1, BOW);
                    inv.setItem(2, new ItemStack(Material.COOKED_BEEF, 16));
                    inv.setItem(9, new ItemStack(Material.ARROW));
                },
                new Material[]{Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS}
        ),
        Demolition(
                pl -> {
                    PlayerInventory inv = pl.getPlayer().getInventory();
                    inv.setItem(0, new ItemStack(Material.IRON_AXE));
                    inv.setItem(1, new ItemStack(Material.COOKED_BEEF, 16));
                    inv.setItem(2, new ItemStack(Material.TNT, 32));
                },
                new Material[]{Material.GOLD_HELMET, Material.LEATHER_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS}
        );

        public final Apply apply;
        public final Material[] armor;

        Class(Apply apply, Material[] armor) {
            this.apply = apply;
            this.armor = armor;
        }
    }

    private void apply(WarPlayer pl, Class clazz) {
        main.items().clear(pl);
        main.items().applyArmorAcccordingToTeam(pl, clazz.armor);
        clazz.apply.apply(pl);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlock().getLocation();
        if (loc.getY() > 83) return;
        if (event.getBlockPlaced().getType() == Material.TNT) {
            event.setCancelled(true); // Cancel to stop the warning message
            event.getPlayer().getInventory().removeItem(new ItemStack(Material.TNT, 1));
            event.getPlayer().getWorld().playSound(loc, Sound.ENTITY_TNT_PRIMED, 1L, 1L);

            // Spawn the tnt and set the player as the source using reflections
            TNTPrimed tnt = loc.getWorld().spawn(loc, TNTPrimed.class);
            tnt.setFuseTicks(30);
            EntityLiving nmsEntityLiving = ((CraftLivingEntity) event.getPlayer()).getHandle();
            EntityTNTPrimed nmsTNT = ((CraftTNTPrimed) tnt).getHandle();
            try {
                Field sourceField = EntityTNTPrimed.class.getDeclaredField("source");
                sourceField.setAccessible(true);
                sourceField.set(nmsTNT, nmsEntityLiving);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;
        event.setCancelled(true);
        for (LivingEntity target : event.getAffectedEntities())
            if (event.getEntity().getShooter().equals(target))
                main.warn((Player) target, "Potions have no effect on yourself on this map");
            else target.addPotionEffects(event.getEntity().getEffects());
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        WarPlayer wp = main.getWarPlayer(event.getPlayer());
        if (!wp.isPlaying()) return;
        Block block = event.getClickedBlock();
        if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
            Sign sign = (Sign) block.getState();
            apply(wp, Class.valueOf(sign.getLine(1)));
        }
    }
}
