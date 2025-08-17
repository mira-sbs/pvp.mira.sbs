package sbs.mira.pvp.framework.util.modules;


import sbs.mira.pvp.framework.MiraPulse;
import sbs.mira.pvp.framework.MiraModule;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Random;

/**
 * This class handles the moving, removal, and creation of
 * the physical worlds on the Minecraft server. This demonstrates
 * file handling, but also contains a lot of Spigot
 * <p>
 * Created by Josh on 18/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see ItemStack
 * @since 1.0
 */
public class WorldUtility extends MiraModule {

    private String repo; // Holds the directory path to the map repository.

    /**
     * World utility constructor.
     * We need to link back to the manager and plugin.
     *
     * @param main The supercontroller.
     */
    public WorldUtility(MiraPulse main) {
        super(main);
        repo = main.plugin().getConfig().getString("settings.maps_repo");
    }

    /**
     * Takes the world file from the map directory, and pastes it
     * into the Bukkit directory so it can be loaded and used.
     * <p>
     * The map directory is located at $PLUGIN/maps/.
     * $PLUGIN is the directory created for the plugin by bukkit in $BUKKIT/plugins/$PLUGIN/.
     *
     * @param map The name of the map.
     * @param ID  The 5-digit ID that the world will be defined as.
     */
    public void loadMap(String map, long ID) {
        mira().plugin().log("Now attempting to load " + map + " to " + ID + "!"); // Debug.
        try {
            File path = new File(repo + File.separator + map);
            mira().plugin().log("Maps repo path: " + path);
            // Non-absolute paths will default to inside the plugin folder.
            copyFolder(path, new File(ID + ""));
            // Attempts to copy over the whole directory so it can be used.
        } catch (IOException e) {
            e.printStackTrace();
            mira().plugin().getServer().shutdown(); // Can't play without a world.
        }

        // Calling upon the Spigot Gods to load our world.
        WorldCreator wc = new WorldCreator(ID + "").generator(new NullChunkGenerator());
        World world = Bukkit.createWorld(wc);
        world.setAutoSave(false); // Since it won't ever be used after the match, don't autosave.
        WorldServer handle = ((CraftWorld) world).getHandle();
        handle.keepSpawnInMemory = false; // Prevent minor memory leak.

        world.getChunkAt(0, 0).load(); // Load the chunk so it's ready to be used!
    }

    /**
     * Once the source file and destination file have been
     * determined, this procedure opens an input and output
     * stream to transfer the data over 1024 bytes at a time.
     *
     * @param src  Source directory.
     * @param dest Destination directory.
     * @throws java.io.IOException Thrown if an error occurs while trying to copy.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void copyFolder(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            // If this file is a directory, copy the files within this directory.
            while (!dest.exists())
                dest.mkdir();
            String files[] = src.list();
            for (String file : files != null ? files : new String[0])
                copyFolder(new File(src, file), new File(dest, file));
        } else {
            // Otherwise copy this file.
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) // While the number of bytes to transfer is more than zero, transfer!
                out.write(buffer, 0, length);
            in.close();
            out.close();
        }
    }

    /**
     * Once a match has finished and cycled, the world file
     * should be destroyed as it is no longer needed and a
     * fresh copy is used when the same map is played again.
     *
     * @param map The world to restore, using the 5-digit ID.
     */
    public void restoreMap(String map) {
        if (Bukkit.getWorld(map) != null) {
            // If the world is still loaded, unload it forcibly and get rid of it.
            mira().plugin().log("Unloading world " + map);
            forceUnloadWorld(Bukkit.getWorld(map));
        }
        try {
            delete(new File(map)); // Bye bye!
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Forcibly unloads a world, even if it refuses to.
     *
     * @param world World to force unload
     */
    private void forceUnloadWorld(World world) {
        mira().plugin().log("Now attempting to forcibly unload world " + world.getName() + "!");

        CraftServer server = (CraftServer) Bukkit.getServer(); // Returns an advanced handle of the Spigot server instance.
        Bukkit.unloadWorld(world, false); // Unload the world using Bukkit.

        try {
            Field f = server.getClass().getDeclaredField("worlds"); // Use reflections to get the list of loaded worlds.
            f.setAccessible(true); // Let us access it.
            @SuppressWarnings("unchecked")
            Map<String, World> worlds = (Map<String, World>) f.get(server); // Extract the worlds key/value set.
            worlds.remove(world.getName().toLowerCase()); // Remove it from the list.
            f.setAccessible(false); // Make it private again.
        } catch (IllegalAccessException | NoSuchFieldException ex) { /* This can't happen. */}
    }

    /**
     * Deletes a directory. This is called when a map
     * world is no longer needed and can be removed.
     * <p>
     * This demonstrates file I/O.
     *
     * @param path Directory to delete.
     */
    @SuppressWarnings("ConstantConditions")
    private void delete(File path) throws IOException {
        if (!path.exists())
            return;
        if (path.isDirectory()) {
            // If this is a directory, delete the files within it.
            File[] toDelete = path.listFiles();
            if (toDelete.length > 0)
                for (File f : toDelete)
                    delete(f); // Delete any files in the directory first.
        }
        // We can delete it now.
        FileUtils.forceDelete(path);
    }

    /**
     * This class is a simple Minecraft world generator.
     * It generates nothing, as maps are built on air.
     * <p>
     * This is used when loading a map world with a generator.
     */
    private static class NullChunkGenerator extends ChunkGenerator {

        public byte[] generate(World world, Random random, int cx, int cz) {
            return new byte[65536];
        }

        @Override
        public Location getFixedSpawnLocation(World world, Random random) {
            return new Location(world, 0, 64, 0);
        }
    }
}