package com.cloudbees.eclipse.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.osgi.framework.Bundle;

import com.cloudbees.eclipse.core.forge.api.ForgeSync;
import com.cloudbees.eclipse.core.internal.forge.ForgeSubclipseSync;
import com.cloudbees.eclipse.core.internal.forge.ForgeSubversiveSync;

/**
 * Main service for syncing Forge repositories and Eclipse repository entries. Currently supported providers are EGit,
 * Sublclipse, Subversive
 * 
 * @author ahtik
 */
public class ForgeSyncService {

  private List<ForgeSync> providers = new ArrayList<ForgeSync>();

  public ForgeSyncService() {
    if (bundleActive("org.tigris.subversion.subclipse.core")) {
      providers.add(new ForgeSubclipseSync());
    }
    if (bundleActive("org.eclipse.team.svn.core")) {
      providers.add(new ForgeSubversiveSync());
    }
  }

  public void addProvider(ForgeSync provider) {
    providers.add(provider);
  }

  public static boolean bundleActive(String bundleName) {
    Bundle bundle = Platform.getBundle(bundleName);
    if (bundle != null && (bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STARTING)) {
      return true;
    }
    return false;
  }

  public void sync(ForgeSync.TYPE type, Properties props, IProgressMonitor monitor) throws CloudBeesException {
    int ticksPerProcess = 100 / providers.size();
    if (ticksPerProcess <= 0) {
      ticksPerProcess = 1;
    }
    for (ForgeSync provider : providers) {
      try {
        provider.sync(type, props, new SubProgressMonitor(monitor, ticksPerProcess));
      } catch (Exception e) {
        CloudBeesCorePlugin.getDefault().getLogger().error(e);
      }
    }
  }

}
