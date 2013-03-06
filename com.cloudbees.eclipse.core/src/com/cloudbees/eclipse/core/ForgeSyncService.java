/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cloudbees.eclipse.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.osgi.framework.Bundle;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.forge.api.ForgeSync;
import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem;
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

  public void updateStatus(final ForgeInstance instance, final IProgressMonitor monitor) throws CloudBeesException {
    int ticksPerProcess = 100 / Math.max(this.providers.size(), 1);
    if (ticksPerProcess <= 0) {
      ticksPerProcess = 1;
    }

    for (ForgeSync provider : this.providers) {
      if (monitor.isCanceled()) {
        throw new OperationCanceledException();
      }

      try {
        provider.updateStatus(instance, new SubProgressMonitor(monitor, ticksPerProcess));
      } catch (Exception e) {
        CloudBeesCorePlugin.getDefault().getLogger().error(e);
        instance.lastException = e;
      }
    }
  }

  public void sync(final ForgeInstance instance, final IProgressMonitor monitor) throws CloudBeesException {
    int ticksPerProcess = 100 / Math.max(this.providers.size(), 1);
    if (ticksPerProcess <= 0) {
      ticksPerProcess = 1;
    }

    for (ForgeSync provider : this.providers) {
      if (monitor.isCanceled()) {
        throw new OperationCanceledException();
      }

      try {
        provider.sync(instance, new SubProgressMonitor(monitor, ticksPerProcess));
      } catch (Exception e) {
        CloudBeesCorePlugin.getDefault().getLogger().error(e);
        instance.lastException = e;
      }
    }
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

  public void addToRepository(final ForgeInstance instance, final IProject project, final IProgressMonitor monitor)
      throws CloudBeesException {
    for (ForgeSync provider : this.providers) {
      provider.addToRepository(instance, project, monitor);
    }
  }

  public boolean isUnderSvnScm(final IProject project) {
    for (ForgeSync provider : this.providers) {
      if (provider.isUnderSvnScm(project)) {
        return true;
      }
    }
    return false;
  }

  public ForgeInstance getSvnRepo(final IProject project) {
    for (ForgeSync provider : this.providers) {
      if (provider.isUnderSvnScm(project)) {
        return provider.getMainRepo(project);
      }
    }
    return null;
  }
}
