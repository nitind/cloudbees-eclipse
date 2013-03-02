package com.cloudbees.eclipse.run.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;

public class ApplicationPoller extends Thread {

  private static final int MINUTE = 60;
  private static final int SECOND = 1000;
  private static int delay = 2 * MINUTE * SECOND;

  private boolean stop = false;

  public ApplicationPoller() {
    super("ApplicationPoller");
  }

  @Override
  public void run() {
    this.stop = false;

    while (!this.stop) {
      try {
        fetchAndUpdateApps();

        Thread.sleep(delay);
      } catch (Exception e) {
        CBRunCoreActivator.logError(e);
        try {
          Thread.sleep(delay);
        } catch (InterruptedException e1) {
          CBRunCoreActivator.logError(e);
        }
      }
    }
  }

  public void fetchAndUpdateApps() throws Exception {
    if (this.stop) {
      return;
    }

    ApplicationListResponse list = BeesSDK.getList();

    updateStatus(list);
    for (ApplicationInfo info : list.getApplications()) {
      updateStatus(info.getId(), info.getStatus(), info);
    }
  }

  /**
   * Update entire list
   * 
   * @param list
   */
  private void updateStatus(ApplicationListResponse list) {
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunCoreActivator.PLUGIN_ID, "statusUpdater").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object executableExtension = element.createExecutableExtension("updater");
          if (executableExtension instanceof ApplicationStatusUpdater) {
            ((ApplicationStatusUpdater) executableExtension).update(list);
          }
        } catch (CoreException e) {
          CBRunCoreActivator.logError(e);
        }
      }
    }

  }

  /**
   * Update one app at a time.
   * 
   * @param id
   * @param status
   * @param info
   */
  private void updateStatus(String id, String status, ApplicationInfo info) {
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunCoreActivator.PLUGIN_ID, "statusUpdater").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object executableExtension = element.createExecutableExtension("updater");
          if (executableExtension instanceof ApplicationStatusUpdater) {
            ((ApplicationStatusUpdater) executableExtension).update(id, status, info);
          }
        } catch (CoreException e) {
          CBRunCoreActivator.logError(e);
        }
      }
    }
  }

  public void halt() {
    this.stop = true;
  }

}
