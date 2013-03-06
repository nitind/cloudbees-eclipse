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
package com.cloudbees.eclipse.dtp;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.gc.api.ClickStartCreateResponse;
import com.cloudbees.eclipse.core.gc.api.ClickStartCreateResponse.Component;
import com.cloudbees.eclipse.dtp.internal.ConnectDatabaseAction;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.NewClickStartProjectHook;

public class NewDatabaseHook implements NewClickStartProjectHook {

  public NewDatabaseHook() {
  }

  public void hookProject(ClickStartCreateResponse resp, IProject project, IProgressMonitor monitor)
      throws com.cloudbees.eclipse.core.CloudBeesException {

    /*System.out.println("New database hook for "+resp);
    System.out.println("Provisioning database for: "+resp.dbManagerUrl);*/
    if (resp.components != null) {
      for (int i = 0; i < resp.components.length; i++) {
        Component c = resp.components[i];
        // System.out.println("COMPONENT: key:"+c.key+"; name:"+c.name+"; url:"+c.url);

        if (c.key != null && c.key.toLowerCase().startsWith("database_")) {
          String dbId = c.key.substring("database_".length());
          try {
            DatabaseInfo dbinfo = BeesSDK.getDatabaseInfo(dbId, true);
            ConnectDatabaseAction.connectNewDatabase(dbinfo);
          } catch (Exception e) {
            throw new CloudBeesException("Failed to connect database", e);
          }
        }

      }
    }
  }

}
