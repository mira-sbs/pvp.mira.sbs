package sbs.mira.pvp.framework;

import org.jetbrains.annotations.NotNull;
import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import sbs.mira.pvp.MiraPvpMaster;
import sbs.mira.pvp.MiraPvpPlugin;
import sbs.mira.pvp.MiraPvpPulse;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * everything good in mira, built on top of JavaPlugin.
 * created on 2017-03-20.
 *
 * @author jj.mira.sbs
 * @version 1.0.1
 * @see org.bukkit.plugin.java.JavaPlugin
 * @since 1.0.0
 */
public abstract
class MiraPlugin<Pulse extends MiraPulse<?, ?>>
  extends JavaPlugin
  implements Breather<Pulse>
{
  
  @Nullable
  private Pulse pulse;
  
  @NotNull
  private final CommandsManager<CommandSender> commands_manager = new CommandsManager<>()
  {
    
    /***
     * @see org.bukkit.permissions.Permission
     */
    @Override
    public
    boolean hasPermission(CommandSender sender, String perm)
    {
      Permission permission = new Permission(perm, PermissionDefault.FALSE);
      return sender instanceof ConsoleCommandSender || sender.hasPermission(permission);
    }
  };
  
  
  private final ArrayList<Class<? extends MiraModule>> module_classes = new ArrayList<>();
  
  
  @Override
  public @NotNull
  Pulse pulse() throws FlatlineException
  {
    if (this.pulse != null)
    {
      return pulse;
    }
    else
    {
      throw new FlatlineException();
    }
  }
  
  @Override
  public
  void breathe(@NotNull Pulse pulse) throws IllegalStateException
  {
    if (this.pulse == null)
    {
      this.pulse = pulse;
    }
    else
    {
      throw new IllegalStateException("a breather may not have two pulses.");
    }
  }
  
  /**
   * log an informational message to the jvm console.
   *
   * @param message yap.
   * @see java.util.logging.Logger
   */
  public
  void log(String message)
  {
    getLogger().log(Level.INFO, "[war] " + message);
  }
  
  /**
   * registers a mira module.
   *
   * @see MiraModule
   * @see com.sk89q.minecraft.util.commands.Command
   */
  protected
  void register_module(Class<? extends MiraModule> module_class)
  {
    // Add the class so it will be initialised later.
    module_classes.add(module_class);
  }
  
  /**
   * registers commands found in the provided plugin module classes.
   *
   * @see MiraPlugin#module_classes
   */
  protected
  void register_module_commands()
  {
    assert !module_classes.isEmpty();
    
    commands_manager.setInjector(new SimpleInjector(pulse()));
    
    CommandsManagerRegistration registration = new CommandsManagerRegistration(this, commands_manager);
    
    module_classes.forEach(registration::register);
  }
  
  /**
   * our lord and saviour sk89q takes the wheel.
   *
   * @return true; always; don't fall back on standard nms logic.
   * @see com.sk89q.minecraft.util.commands.Command
   */
  @Override
  public
  boolean onCommand(
    @NotNull CommandSender sender,
    @NotNull Command command,
    @NotNull String label,
    @NotNull String[] arguments
  )
  {
    try
    {
      // Execute it through sk89q's command processor.
      commands_manager.execute(command.getName(), arguments, sender, sender);
    }
    catch (CommandPermissionsException e)
    {
      // No permission?
      sender.sendMessage(pulse().master().message("command.validation.error.permission"));
    }
    catch (MissingNestedCommandException e)
    {
      sender.sendMessage(pulse().master().message("command.validation.error.generic", e.getUsage()));
    }
    catch (CommandUsageException e)
    {
      sender.sendMessage(pulse().master().message("command.validation.error.usage", e.getMessage(), e.getUsage()));
    }
    catch (WrappedCommandException e)
    {
      if (e.getCause() instanceof NumberFormatException)
      {
        sender.sendMessage(pulse().master().message("command.validation.error.number.format"));
      }
      else
      {
        sender.sendMessage(pulse()
                             .master()
                             .message("command.validation.error.generic", "unknown error: " + e.getMessage()));
        //noinspection CallToPrintStackTrace
        e.printStackTrace();
      }
    }
    catch (CommandException e)
    {
      sender.sendMessage(pulse().master().message("command.validation.error.generic", e.getMessage()));
    }
    return true;
  }
}
