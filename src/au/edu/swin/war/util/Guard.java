package au.edu.swin.war.util;

import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.framework.util.WarMatch;
import au.edu.swin.war.framework.util.WarModule;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.EntityTNTPrimed;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftTNTPrimed;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.lang.reflect.Field;

/**
 * This class listens for certain Spigot events in
 * certain scenarios and blocks/acts upon them.
 * <p>
 * Created by Josh on 20/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @since 1.0
 */
public class Guard extends WarModule implements Listener {

    Guard(WarManager main) {
        super(main);
        main().plugin().getServer().getPluginManager().registerEvents(this, main().plugin());
    }

    /*
     * ALL EVENTS BELOW ARE FOR BLOCKS.
     */

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    @EventHandler
    public void onPlace(BlockDamageEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    //TODO: projectile launch event

    /*
     * ALL EVENTS BELOW ARE FOR PLAYERS.
     */
    @EventHandler
    public void onArmorStandChange(PlayerArmorStandManipulateEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), false));
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), false));
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), false));
    }

    @EventHandler
    public void onPortalEvent(PlayerPortalEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onShear(PlayerShearEntityEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    /*
     * ALL EVENTS BELOW ARE FOR ENTITIES.
     */

    @EventHandler
    public void onLinger(AreaEffectCloudApplyEvent event) {
        event.getAffectedEntities().removeIf(livingEntity -> !main().match().canInteract(livingEntity, false));
    }

    @EventHandler
    public void onAir(EntityAirChangeEvent event) {
        event.setCancelled(!main().match().canInteract(event.getEntity(), false));
    }

    @EventHandler
    public void onCombust(EntityCombustEvent event) {
        event.setCancelled(!main().match().canInteract(event.getEntity(), false));
    }

    @EventHandler
    public void onCreatePortal(EntityCreatePortalEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player)
            if (((Player) event.getEntity()).getGameMode() == GameMode.SPECTATOR) {
                event.setCancelled(true);
                return;
            }
        event.setCancelled(!main().match().canInteract(event.getEntity(), false));
    }

    @EventHandler
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Projectile && ((Projectile) event.getDamager()).getShooter() instanceof Player)
            event.setCancelled(!main().match().canInteract((Player) ((Projectile) event.getDamager()).getShooter(), true));
        else
            event.setCancelled(!main().match().canInteract(event.getDamager(), true));
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (main().match().getStatus() != WarMatch.Status.PLAYING)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {
        event.setCancelled(!main().match().canInteract(event.getEntity(), false));
        if (event.getEntity() instanceof Player && !event.isCancelled() && (boolean) main().cache().getCurrentMap().attr().get("itemMerging"))
            tryItemMerge(event);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        event.setCancelled(!main().match().canInteract(event.getWhoClicked(), true));
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        event.setCancelled(!main().match().canInteract(event.getTarget(), false));
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Entity)
            event.setCancelled(!main().match().canInteract((Entity) event.getEntity().getShooter(), true));
    }

    @EventHandler
    public void onSplash(PotionSplashEvent event) {
        event.getAffectedEntities().removeIf(livingEntity -> !main().match().canInteract(livingEntity, false));
    }

    @EventHandler
    public void onLeash(PlayerLeashEntityEvent event) {
        event.setCancelled(!main().match().canInteract(event.getEntity(), false));
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof TippedArrow)
            event.getEntity().remove();
    }

    /*
     * ALL EVENTS BELOW ARE FOR HANGING ENTITIES.
     */

    @EventHandler
    public void on(HangingBreakByEntityEvent event) {
        event.setCancelled(!main().match().canInteract(event.getEntity(), true));
    }

    @EventHandler
    public void on(HangingPlaceEvent event) {
        event.setCancelled(!main().match().canInteract(event.getPlayer(), true));
    }

    /*
     * ALL EVENTS BELOW ARE FOR VEHICLES AND WEATHER.
     */

    @EventHandler
    public void onVehicleDamage(VehicleEnterEvent event) {
        event.setCancelled(!main().match().canInteract(event.getEntered(), true));
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        event.setCancelled(!main().match().canInteract(event.getAttacker(), true));
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        event.setCancelled(!main().match().canInteract(event.getAttacker(), true));
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    /*
     * ALL EVENTS BELOW ARE FOR LAPIS AUTOENCHANT.
     */
    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getInventory() instanceof EnchantingInventory)
            event.getInventory().setItem(1, new ItemStack(Material.INK_SACK, 3, (short) 4));
    }

    @EventHandler
    public void onEnchclick(InventoryClickEvent event) {
        if (event.getClickedInventory() instanceof EnchantingInventory) {
            if (event.isShiftClick() && event.getSlot() == 1)
                event.setCancelled(true);
            else if (event.getSlot() == 1)
                event.setCurrentItem(new ItemStack(Material.AIR));
        }
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        if (event.getInventory() instanceof EnchantingInventory)
            event.getInventory().setItem(1, new ItemStack(Material.INK_SACK, 3, (short) 4));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory() instanceof EnchantingInventory)
            event.getInventory().setItem(1, null);
    }

    /**
     * Spawns a TNT and sets an entity as the source.
     *
     * @param source   Source of the TNT ignition.
     * @param location Location to spawn TNT.
     */
    @Deprecated
    private void spawnTNT(LivingEntity source, Location location) {
        TNTPrimed tnt = location.getWorld().spawn(location.add(0.5, 0, 0.5), TNTPrimed.class);

        // Change via NMS the source of the TNT by the player
        EntityLiving nmsSource = ((CraftLivingEntity) source).getHandle();
        EntityTNTPrimed nmsTNT = ((CraftTNTPrimed) tnt).getHandle();
        try {
            Field sourceField = EntityTNTPrimed.class.getDeclaredField("source");
            sourceField.setAccessible(true);
            sourceField.set(nmsTNT, nmsSource);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Bukkit.broadcastMessage("tnt spawn");
    }

    /**
     * Tries to merge an item, if it has durability.
     *
     * @param event Event called by spigot.
     */

    private void tryItemMerge(EntityPickupItemEvent event) {
        Player pl = (Player) event.getEntity();
        ItemStack toMerge = event.getItem().getItemStack();
        if (toMerge.getType().getMaxDurability() == 0) return;
        for (ItemStack armor : pl.getInventory().getArmorContents())
            if (canMerge(armor, toMerge)) {
                merge(event, armor, toMerge);
                return;
            }
        for (ItemStack item : pl.getInventory().getContents())
            if (canMerge(item, toMerge)) {
                merge(event, item, toMerge);
                return;
            }
        pl.updateInventory();
    }

    /**
     * Performs a few simple checks to see if these items can merge.
     *
     * @param compare Stack to compare against.
     * @param stack   Stack to merge with comparison.
     * @return Whether or not these items can merge.
     */
    private boolean canMerge(ItemStack compare, ItemStack stack) {
        return compare != null && compare.getType().getMaxDurability() >= 1 && compare.getTypeId() == stack.getTypeId() && compare.hasItemMeta() == stack.hasItemMeta() && (compareMeta(compare, stack));
    }

    /**
     * Compares item metas.
     * This method temporarily changes the color of leather armor
     * to resemble each other so that they can still match. Most
     * teams do not have the same color armor.
     *
     * @param compare Stack to compare against.
     * @param stack   Stack to merge with comparison.
     * @return Whether or not these items match in meta.
     */
    private boolean compareMeta(ItemStack compare, ItemStack stack) {
        compare = compare.clone();
        stack = stack.clone();
        if (compare.getItemMeta() instanceof LeatherArmorMeta) {
            LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
            meta.setColor(((LeatherArmorMeta) compare.getItemMeta()).getColor());
            stack.setItemMeta(meta);
        }
        return !compare.hasItemMeta() || Bukkit.getItemFactory().equals(compare.getItemMeta(), stack.getItemMeta());
    }

    /**
     * After a check, it will merge the two items together.
     * It takes the free durability of the item picked up and
     * adds it to a vacant matching item and essentially 'repairs' it.
     *
     * @param event     Event called by spigot.
     * @param mergeTo   Item to merge with.
     * @param mergeFrom Item to merge from.
     */
    private void merge(EntityPickupItemEvent event, ItemStack mergeTo, ItemStack mergeFrom) {
        event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5F, 2F);
        event.getItem().remove();
        event.setCancelled(true);
        short newDura = mergeTo.getDurability();
        newDura -= mergeFrom.getType().getMaxDurability() - mergeFrom.getDurability();
        if (newDura <= 0) newDura = 0;
        mergeTo.setDurability(newDura);
    }
}
