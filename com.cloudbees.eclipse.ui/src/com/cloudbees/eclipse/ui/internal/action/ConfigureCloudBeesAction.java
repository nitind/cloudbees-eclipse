package com.cloudbees.eclipse.ui.internal.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class ConfigureCloudBeesAction extends Action {

  public ConfigureCloudBeesAction() {
    super();
    setText("Configure CloudBees access...");
    setToolTipText("Configure CloudBees account access");
    /*    action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
            .getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
    */

  }

  public void run() {
    PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(null,
        "com.cloudbees.eclipse.ui.preferences.GeneralPreferencePage", new String[] {
            "com.cloudbees.eclipse.ui.preferences.JenkinsInstancesPreferencePage",
            "com.cloudbees.eclipse.ui.preferences.GeneralPreferencePage" }, null);
    if (pref != null) {
      pref.open();
    }
  }

}
