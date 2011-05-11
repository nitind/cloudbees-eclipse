package com.cloudbees.eclipse.dev.scm.subclipse;

import com.cloudbees.eclipse.core.ForgeSyncService;
import com.cloudbees.eclipse.core.forge.api.ForgeSyncEnabler;

public class ForgeSubclipseSyncEnabler implements ForgeSyncEnabler {

  @Override
  public boolean isEnabled() {
    boolean enabled = ForgeSyncService.bundleActive("org.tigris.subversion.subclipse.core");
    return enabled;
  }

  @Override
  public String getName() {
    return "Subclipse";
  }

}
