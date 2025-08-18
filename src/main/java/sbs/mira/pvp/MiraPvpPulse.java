package sbs.mira.pvp;

import org.jetbrains.annotations.NotNull;
import sbs.mira.core.MiraPulse;

public
final
class MiraPvpPulse
  extends MiraPulse<MiraPvpPlugin, MiraPvpMaster>
{
  public
  MiraPvpPulse(
    @NotNull MiraPvpPlugin plugin,
    @NotNull MiraPvpMaster master
  )
  {
    super(plugin, master);
  }
}
