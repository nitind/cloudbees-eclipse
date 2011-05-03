package com.cloudbees.eclipse.run.core;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;

public class ApplicationPoller extends Thread {

  private static int delay = 60 * 1000 * 60; // once a hour

  private boolean stop = false;

  private final HashMap<String, ApplicationInfo> lastState = new HashMap<String, ApplicationInfo>();

  public ApplicationPoller() {
    super("ApplicationPoller");
  }

  @Override
  public void run() {
    this.stop = true; //FIXME: this poller is not yet used, lets disable it first.

    while (!this.stop) {
      try {
        pollLoop();
      } catch (Exception e) {
        e.printStackTrace();
        CBRunCoreActivator.logError(e);
      }
    }
  }

  private void pollLoop() throws Exception {
    ApplicationListResponse list = BeesSDK.getList();
    for (ApplicationInfo info : list.getApplications()) {

      String id = info.getId();
      ApplicationInfo aInfo = this.lastState.get(id);

      if (aInfo != null && aInfo.getStatus().equals(info.getStatus())) {
        updateStatus(id, info.getStatus(), info);
      }

      this.lastState.put(id, info);
    }
    Thread.sleep(delay);
  }

  private void updateStatus(String id, String status, ApplicationInfo info) {
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunCoreActivator.PLUGIN_ID, "statusUpdater").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object executableExtension = element.createExecutableExtension("updater");
          if (executableExtension instanceof IStatusUpdater) {
            ((IStatusUpdater) executableExtension).update(id, status, info);
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
