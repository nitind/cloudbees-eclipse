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
    store.setDefault(PreferenceConstants.P_ENABLE_FORGE, true);
    store.setDefault(PreferenceConstants.P_ENABLE_JAAS, true);
    store.setDefault(PreferenceConstants.P_JENKINS_REFRESH_ENABLED, true);
    store.setDefault(PreferenceConstants.P_JENKINS_REFRESH_INTERVAL, 60);
    //store.setDefault(PreferenceConstants.P_ACTIVE_ACCOUNT, "");
  }

}
