package com.cloudbees.eclipse.dev.scm.subversive;

import com.cloudbees.eclipse.core.ForgeSyncService;
import com.cloudbees.eclipse.core.forge.api.ForgeSyncEnabler;

public class ForgeSubversiveSyncEnabler implements ForgeSyncEnabler {

  @Override
  public boolean isEnabled() {
    boolean enabled = ForgeSyncService.bundleActive("org.eclipse.team.svn.core");
    return enabled;
  }

  @Override
  public String getName() {
    return "Subversive";
  }

}
