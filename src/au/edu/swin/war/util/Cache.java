package au.edu.swin.war.util;

import au.edu.swin.war.framework.util.WarCache;
import au.edu.swin.war.framework.util.WarManager;

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
    protected Cache(Manager main) {
        super(main);
    }

    @Override
    public void loadGamemodes() {

    }

    @Override
    public void loadMaps() {

    }
}
