package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ConfigureSshKeysAction extends CBTreeAction {

  public ConfigureSshKeysAction() {
    super();
    setText("Configure SSH keys...");
    setToolTipText("Configure SSH keys used to access the git or svn repositories");

    /*    setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
     */
  }

  @Override
  public void run() {
    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
      @Override
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

    CloudBeesUIPlugin.getDefault().openWithBrowser("https://grandcentral.cloudbees.com/account/edit");

    if (pref != null) {
      pref.open();
    }
  }

  @Override
  public boolean isPopup() {
    return false;
  }

  @Override
  public boolean isPullDown() {
    return true;
  }

  @Override
  public boolean isToolbar() {
    return false;
  }
}
