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
package com.cloudbees.eclipse.ui.internal;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ConfigureSshKeysAction extends CBTreeAction {

  public ConfigureSshKeysAction() {
    super(false);
    setText("SSH Key Setup Help");
    setToolTipText("Configure SSH keys used to access the git or svn repositories");

    /*    setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
     */
  }

  @Override
  public void run() {
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

      public void run() {
        MessageDialog
            .openInformation(
                CloudBeesUIPlugin.getActiveWindow().getShell(),
                "Configure SSH keys",
                "In order to access Git or SVN via SSH you need to configure public-private keys.\n\n"
                    + "In the next step the Eclipse SSH preferences page will open. Also in the browser will be open the corresponding 'User settings' configuration page at CloudBees site.\n\n"
                    + "Either load an existing key or generate a new one on the 'Key Management' tab and then copy-paste the public key to the browser into the CloudBees 'User settings/Public Key' field.");
      }
    });

    PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(CloudBeesUIPlugin.getActiveWindow().getShell(),
        "org.eclipse.jsch.ui.SSHPreferences", new String[] { "org.eclipse.ui.net.NetPreferences",
            "org.eclipse.jsch.ui.SSHPreferences" }, null);

    CloudBeesUIPlugin.getDefault().openWithBrowser(GrandCentralService.GC_BASE_URL+"/account/edit");

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
