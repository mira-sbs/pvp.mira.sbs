package sbs.mira.pvp;

import org.jetbrains.annotations.NotNull;
import sbs.mira.pvp.framework.MiraPulse;

public interface MiraPvp extends MiraPulse
{
  @Override
  @NotNull
  MiraPvpPlugin plugin();
  
  @Override
  @NotNull
  MiraPvpMaster master();
}
