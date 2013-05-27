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
package com.cloudbees.eclipse.ui.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.PreferenceConstants;

/**
 * Initializes default preference values.
 * 
 * @author ahtik
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
   */
  public void initializeDefaultPreferences() {
    IPreferenceStore store = CloudBeesUIPlugin.getDefault().getPreferenceStore();
    
    store.setDefault(PreferenceConstants.P_GITPROTOCOL, "HTTPS");
    store.setDefault(PreferenceConstants.P_JENKINS_REFRESH_ENABLED, true);
    store.setDefault(PreferenceConstants.P_JENKINS_REFRESH_INTERVAL, 60);
    //store.setDefault(PreferenceConstants.P_ACTIVE_ACCOUNT, "");
  }

}
