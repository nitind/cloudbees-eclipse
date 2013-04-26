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

  @Override
  public String getName() {
    return "EGit";
  }

}
