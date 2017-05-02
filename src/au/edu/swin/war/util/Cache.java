package au.edu.swin.war.util;

import au.edu.swin.war.framework.util.WarCache;
import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.game.Gamemode;
import au.edu.swin.war.game.Map;
import au.edu.swin.war.game.modes.*;
import au.edu.swin.war.maps.*;

/**
 * An extension to WarCache.
 * Acts as a cache for all maps/gamemodes.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see WarManager
 * <p>
 * Created by Josh on 20/04/2017.
 * @since 1.0
 */
public class Cache extends WarCache {

    /**
     * Constructor of the extended Cache.
     * Everything that should be done is
     * done in the WarCache constructor.
     *
     * @param main The supercontroller.
     */
    Cache(Manager main) {
        super(main);
    }

    @Override
    public void loadGamemodes() {
        loadGamemode(TDM.class);
        loadGamemode(KoTH.class);
        loadGamemode(CTF.class);
        loadGamemode(LMS.class);
        loadGamemode(FFA.class);
        loadGamemode(DDM.class);
        loadGamemode(DTM.class);
        loadGamemode(LTS.class);
        loadGamemode(LP.class);
    }

    @Override
    public void loadMaps() {
        loadMap(TestMap1.class);
        loadMap(TestMap2.class);
        loadMap(TestMap3.class);
        loadMap(TestMap4.class);
        loadMap(TestMap5.class);
    }

    /**
     * Instantiates a map class and initialises it.
     * Also puts into the map key/value set.
     *
     * @param toLoad The map to load.
     */
    private void loadMap(Class<? extends Map> toLoad) {
        try {
            Map result = toLoad.newInstance();
            result.init(main());
            maps.put(result.getMapName(), result);
            main().plugin().log("Map initialised and stored: " + result.getMapName());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Instantiates a gamemode class and initialises it.
     * Also puts into the gamemode key/value set.
     *
     * @param toLoad The gamemode to load.
     */
    private void loadGamemode(Class<? extends Gamemode> toLoad) {
        try {
            Gamemode result = toLoad.newInstance();
            result.init(main());
            gamemodes.put(result.getFullName(), result);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
