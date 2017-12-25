package au.edu.swin.war.util.modules;


import au.edu.swin.war.framework.util.WarManager;
import au.edu.swin.war.framework.util.WarModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;

/**
 * This class handles all procedures or functions
 * relating to the inbuilt bukkit configuration
 * system. Values in config.yml will be handled,
 * stored, and accessed through here as needed.
 * <p>
 * Created by Josh on 25/04/2017.
 *
 * @author s101601828 @ Swin.
 * @version 1.0
 * @see org.bukkit.configuration.Configuration
 * @since 1.0
 */
public class ConfigUtility extends WarModule {

    private final FileConfiguration config; // The configuration of config.yml in YAML.
    private FileConfiguration messages; // The internal messages.yml file.
    /* For pretty code, values will be public in all caps. */
    public boolean WEBSTATS_ENABLED; // Are the stats enabled?
    public String WEBSTATS_ACTION; // Where should it HTTP POST to?
    public String WEBSTATS_SECRET; // What's the secret password to make the script run?
    public int WEBSTATS_POS; // The current match number.

    /**
     * Config utility constructor.
     * We need to link back to the manager and plugin.
     *
     * @param main The supercontroller.
     */
    public ConfigUtility(WarManager main) {
        super(main);

        // Save the default config in case it has never existed before.
        main.plugin().saveDefaultConfig();

        // Reload and assign the file config.
        main.plugin().reloadConfig();
        config = main.plugin().getConfig();

        try {
            messages = YamlConfiguration.loadConfiguration(new InputStreamReader(main().plugin().getResource("messages.yml"), "UTF8"));
        } catch (Exception any) {
            main.plugin().log("The messages were not able to be loaded.");
            Bukkit.shutdown();
        }

        try {
            WEBSTATS_ENABLED = config.getBoolean("webstats.enable");
            WEBSTATS_ACTION = config.getString("webstats.action");
            WEBSTATS_SECRET = config.getString("webstats.secret");
            WEBSTATS_POS = config.getInt("webstats.position");
        } catch (Exception any) {
            main.plugin().log("The configuration was not able to be loaded.");
            WEBSTATS_ENABLED = false;
            WEBSTATS_ACTION = "";
            WEBSTATS_SECRET = "";
            WEBSTATS_POS = -1;
        }
    }

    /**
     * Increment the match position, or ID.
     */
    public void incrementPosition() {
        WEBSTATS_POS++; // Increment the local value.
        config.set("webstats.position", WEBSTATS_POS); // Set the new position in the config.
        main().plugin().saveConfig(); // Save the config.
    }

    /**
     * Returns a message value from a selected key.
     *
     * @param key The key.
     * @return The value.
     */
    public String getMessage(String key) {
        return messages.getString(key);
    }
}