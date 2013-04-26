/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.dtp.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.datatools.connectivity.ConnectionProfileConstants;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.ProfileManager;

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

    deleteNonExistingConnectionProfiles(list, monitor);

  }

  private void deleteNonExistingConnectionProfiles(DatabaseListResponse list, IProgressMonitor monitor) {
    // Construct a list of DB names to match.
    List<DatabaseInfo> dblist = list.getDatabases();
    try {
      if (list == null || dblist == null) {
        return;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    Set<String> dbset = new HashSet<String>();
    for (DatabaseInfo db : dblist) {
      String name = db.getOwner() + "/" + db.getName();
      dbset.add(name);
    }

    List<IConnectionProfile> toRemove = new ArrayList<IConnectionProfile>();
    for (IConnectionProfile profile : ProfileManager.getInstance().getProfiles()) {

      String prop = profile.getBaseProperties().getProperty(ConnectionProfileConstants.PROP_DRIVER_DEFINITION_ID);
      if (prop != null && prop.equalsIgnoreCase(ConnectDatabaseAction.DRIVER_DEF_ID) && profile.getName() != null
          && !dbset.contains(profile.getName())) {
        // profile exists but the corresponding database in the cloud is not there.
        toRemove.add(profile);
      }
    }

    for (IConnectionProfile profile : toRemove) {
      try {
        try {
          profile.disconnect();
        } finally {
          ProfileManager.getInstance().deleteProfile(profile);
        }
      } catch (Exception e) {
        e.printStackTrace();
        // semisilently ignore
      }
    }

    if (toRemove.size() > 0) {
      try {
        CloudBeesDataToolsPlugin.getPoller().fetchAndUpdateDatabases(monitor);
        CloudBeesUIPlugin.getDefault().fireDatabaseInfoChanged();
      } catch (Exception e) {
        e.printStackTrace();
        // semisilently ignore        
      }
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
