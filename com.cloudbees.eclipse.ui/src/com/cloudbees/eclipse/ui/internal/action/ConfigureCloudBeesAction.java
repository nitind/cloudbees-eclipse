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
package com.cloudbees.eclipse.ui.internal.action;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ConfigureCloudBeesAction extends CBTreeAction {

  public ConfigureCloudBeesAction() {
    super(false);
    setText("CloudBees Account...");
    setToolTipText("Configure CloudBees account access");
    /*    action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */

  }

  @Override
  public void run() {
    PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(null,
        "com.cloudbees.eclipse.ui.preferences.JenkinsInstancesPreferencePage", new String[] {
            "com.cloudbees.eclipse.ui.preferences.GeneralPreferencePage",
            "com.cloudbees.eclipse.ui.preferences.JenkinsInstancesPreferencePage"
             }, null);
    if (pref != null) {
      pref.open();
    }
  }

  public boolean isPopup() {
    return false;
  }

  public boolean isPullDown() {
    return true;
  }

  public boolean isToolbar() {
    return false;
  }

}
