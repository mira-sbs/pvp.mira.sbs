package sbs.mira.pvp.framework.util;


import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import sbs.mira.pvp.framework.MiraModule;
import sbs.mira.pvp.framework.MiraPulse;

import java.io.InputStreamReader;

/**
 * this class handles all procedures or functions
 * relating to the inbuilt bukkit configuration
 * system. values in config.yml will be handled,
 * stored, and accessed through here as needed.
 * created on 2017-04-25.
 *
 * @author jj.mira.sbs
 * @author jd.mira.sbs
 * @version 1.0.1
 * @see org.bukkit.configuration.Configuration
 * @since 1.0.0
 */
public
class MiraConfiguration
  extends MiraModule
{
  
  private final FileConfiguration config;
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
  public
  MiraConfiguration(MiraPulse main)
  {
    super(main);
    
    // Save the default config in case it has never existed before.
    main.plugin().saveDefaultConfig();
    
    // Reload and assign the file config.
    main.plugin().reloadConfig();
    config = main.plugin().getConfig();
    
    try
    {
      messages = YamlConfiguration.loadConfiguration(new InputStreamReader(
        mira().plugin().getResource("messages.yml"),
        StandardCharsets.UTF_8
      ));
    }
    catch (Exception any)
    {
      main.plugin().log("The messages were not able to be loaded.");
      Bukkit.shutdown();
    }
    
    try
    {
      WEBSTATS_ENABLED = config.getBoolean("webstats.enable");
      WEBSTATS_ACTION = config.getString("webstats.action");
      WEBSTATS_SECRET = config.getString("webstats.secret");
      WEBSTATS_POS = config.getInt("webstats.position");
    }
    catch (Exception any)
    {
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
  public
  void incrementPosition()
  {
    WEBSTATS_POS++; // Increment the local value.
    config.set("webstats.position", WEBSTATS_POS); // Set the new position in the config.
    mira().plugin().saveConfig(); // Save the config.
  }
  
  /**
   * Returns a message value from a selected key.
   *
   * @param key The key.
   * @return The value.
   */
  public
  String getMessage(String key)
  {
    return messages.getString(key);
  }
}