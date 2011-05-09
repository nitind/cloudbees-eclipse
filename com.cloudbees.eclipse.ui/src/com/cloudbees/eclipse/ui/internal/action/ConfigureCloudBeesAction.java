package com.cloudbees.eclipse.ui.internal.action;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ConfigureCloudBeesAction extends CBTreeAction {

  public ConfigureCloudBeesAction() {
    super();
    setText("CloudBees Account...");
    setToolTipText("Configure CloudBees account access");
    /*    action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */

  }

  @Override
  public void run() {
    PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(null,
        "com.cloudbees.eclipse.ui.preferences.GeneralPreferencePage", new String[] {
            "com.cloudbees.eclipse.ui.preferences.JenkinsInstancesPreferencePage",
            "com.cloudbees.eclipse.ui.preferences.GeneralPreferencePage" }, null);
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
