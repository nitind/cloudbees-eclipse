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
package com.cloudbees.eclipse.ui.internal.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ConfigureAccountHandler extends AbstractHandler {

  public Object execute(ExecutionEvent event) throws ExecutionException {
    PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(null,
        "com.cloudbees.eclipse.ui.preferences.JenkinsInstancesPreferencePage", new String[] {
            "com.cloudbees.eclipse.ui.preferences.JenkinsInstancesPreferencePage",
            "com.cloudbees.eclipse.ui.preferences.GeneralPreferencePage" }, null);

    if (pref != null) {
      pref.open();
    }

    return null;
  }

}
