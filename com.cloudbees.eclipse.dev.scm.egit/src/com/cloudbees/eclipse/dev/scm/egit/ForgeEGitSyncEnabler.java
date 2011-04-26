package com.cloudbees.eclipse.dev.scm.egit;

import com.cloudbees.eclipse.core.ForgeSyncService;
import com.cloudbees.eclipse.core.forge.api.ForgeSyncEnabler;

public class ForgeEGitSyncEnabler implements ForgeSyncEnabler {

  @Override
  public boolean isEnabled() {
    boolean enabled = (ForgeSyncService.bundleActive("org.eclipse.egit.core") || ForgeSyncService
        .bundleActive("org.eclipse.egit")) && ForgeSyncService.bundleActive("org.eclipse.jgit");
    return enabled;
  }

}
