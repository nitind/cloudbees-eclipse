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
package com.cloudbees.eclipse.run.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.gc.api.ClickStartCreateResponse;
import com.cloudbees.eclipse.run.core.CBRunCoreActivator;
import com.cloudbees.eclipse.run.core.NewClickStartProjectHook;

public class NewAppHook implements NewClickStartProjectHook {

  public void hookProject(ClickStartCreateResponse resp, IProject project, IProgressMonitor monitor)
      throws CloudBeesException {
    try {
      CBRunCoreActivator.getPoller().fetchAndUpdateApps();
    } catch (Exception e) {
      throw new CloudBeesException("Failed to reload apps", e);
    }
  }

}
