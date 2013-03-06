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
package com.cloudbees.eclipse.dtp.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.api.DatabaseListResponse;
import com.cloudbees.eclipse.dtp.CloudBeesDataToolsPlugin;
import com.cloudbees.eclipse.dtp.internal.treeview.DatabaseStatusHandler;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class DatabasePoller extends Thread {

  private static final int MINUTE = 60;
  private static final int SECOND = 1000;
  private static int delay = 2 * MINUTE * SECOND; // poll once per 2 minutes

  private boolean stop = false;

  public DatabasePoller() {
    super("DatabasePoller");
  }

  @Override
  public void run() {
    this.stop = false;

    while (!this.stop) {
      try {
        fetchAndUpdateDatabases(new NullProgressMonitor());

        Thread.sleep(delay);
      } catch (Exception e) {
        CloudBeesDataToolsPlugin.logError(e);
        try {
          Thread.sleep(delay);
        } catch (InterruptedException e1) {
          CloudBeesDataToolsPlugin.logError(e);
        }
      }
    }
  }

  public void fetchAndUpdateDatabases(IProgressMonitor monitor) throws Exception {
    if (this.stop) {
      return;
    }

    String account = CloudBeesUIPlugin.getDefault().getActiveAccountName(monitor);
    
    DatabaseListResponse list = BeesSDK.getDatabaseList(account);

    updateStatus(list);
    for (DatabaseInfo info : list.getDatabases()) {
      updateStatus(info.getName(), info.getStatus(), info);
    }
  }

  /**
   * Update entire list
   * 
   * @param list
   */
  private void updateStatus(DatabaseListResponse list) {
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CloudBeesDataToolsPlugin.PLUGIN_ID, "statusUpdater").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object executableExtension = element.createExecutableExtension("updater");
          if (executableExtension instanceof DatabaseStatusUpdate) {
            ((DatabaseStatusUpdate) executableExtension).update(list);
          }
        } catch (CoreException e) {
          CloudBeesDataToolsPlugin.logError(e);
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
  private void updateStatus(String id, String status, DatabaseInfo info) {
    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CloudBeesDataToolsPlugin.PLUGIN_ID, "statusUpdater").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        try {
          Object executableExtension = element.createExecutableExtension("updater");
          if (executableExtension instanceof DatabaseStatusUpdate) {
            ((DatabaseStatusHandler) executableExtension).update(id, status, info);
          }
        } catch (CoreException e) {
          CloudBeesDataToolsPlugin.logError(e);
        }
      }
    }
  }

  public void halt() {
    this.stop = true;
  }

}
