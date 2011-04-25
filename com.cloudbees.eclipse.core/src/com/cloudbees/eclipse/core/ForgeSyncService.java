package com.cloudbees.eclipse.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.osgi.framework.Bundle;

import com.cloudbees.eclipse.core.forge.api.ForgeSync;
import com.cloudbees.eclipse.core.forge.api.ForgeSync.ChangeSetPathItem;
import com.cloudbees.eclipse.core.forge.api.ForgeSync.TYPE;
import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse.AccountServices.ForgeService.Repo;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;

/**
 * Main service for syncing Forge repositories and Eclipse repository entries. Currently supported providers are EGit,
 * Sublclipse, Subversive
 * 
 * @author ahtik
 */
public class ForgeSyncService {

  private final List<ForgeSync> providers = new ArrayList<ForgeSync>();

  public ForgeSyncService() {
  }

  public void addProvider(final ForgeSync provider) {
    this.providers.add(provider);
  }

  public static boolean bundleActive(final String bundleName) {
    Bundle bundle = Platform.getBundle(bundleName);
    if (bundle != null && (bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STARTING)) {
      return true;
    }
    return false;
  }

  public String[] sync(final ForgeSync.TYPE type, final Properties props, final IProgressMonitor monitor)
      throws CloudBeesException {
    int ticksPerProcess = 100 / Math.max(this.providers.size(), 1);
    if (ticksPerProcess <= 0) {
      ticksPerProcess = 1;
    }
    List<String> status = new ArrayList<String>();
    for (ForgeSync provider : this.providers) {
      if (monitor.isCanceled()) {
        throw new OperationCanceledException();
      }

      try {
        ForgeSync.ACTION act = provider.sync(type, props, new SubProgressMonitor(monitor, ticksPerProcess));
        if (!ForgeSync.ACTION.SKIPPED.equals(act)) {
          status.add(act.getLabel() + " " + type + ": " + props.getProperty("url"));
        }
      } catch (Exception e) {
        CloudBeesCorePlugin.getDefault().getLogger().error(e);
      }
    }

    return status.toArray(new String[status.size()]);
  }

  public boolean openRemoteFile(final JenkinsScmConfig scmConfig, final ChangeSetPathItem item,
      final IProgressMonitor monitor) throws CloudBeesException {
    int ticksPerProcess = 100 / Math.max(this.providers.size(), 1);
    if (ticksPerProcess <= 0) {
      ticksPerProcess = 1;
    }
    for (ForgeSync provider : this.providers) {
      if (monitor.isCanceled()) {
        throw new OperationCanceledException();
      }
      try {
        boolean opened = provider.openRemoteFile(scmConfig, item, new SubProgressMonitor(monitor, ticksPerProcess));
        if (opened) {
          return true;
        }
      } catch (Exception e) {
        CloudBeesCorePlugin.getDefault().getLogger().error(e);
      }
    }

    return false;
  }

  public void addToRepository(TYPE type, Repo repo, IProject project, IProgressMonitor monitor)
      throws CloudBeesException {
    for (ForgeSync provider : this.providers) {
      provider.addToRepository(type, repo, project, monitor);
    }
  }

  public boolean isUnderSvnScm(IProject project) {
    for (ForgeSync provider : this.providers) {
      if (provider.isUnderSvnScm(project)) {
        return true;
      }
    }
    return false;
  }

  public Repo getSvnRepo(IProject project) {
    for (ForgeSync provider : this.providers) {
      if (provider.isUnderSvnScm(project)) {
        return provider.getSvnRepo(project);
      }
    }
    return null;
  }
}
