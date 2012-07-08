package com.cloudbees.eclipse.ui.internal;

import org.eclipse.jface.action.Action;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class SelectAccountAction extends Action {

  private String accountName;

  public SelectAccountAction(String accountName, boolean checked) {
    super();
    this.accountName = accountName;
    
/*    params.label = "grandomstate";
    params.mnemonic = "V";
    params.tooltip = "Open CloudBees View";

    Map p = new HashMap();
    //params.parameters = p;
    params.icon = CloudBeesUIPlugin.getImageDescription(CBImages.ICON_16X16_CB_PLAIN);*/
    setText(accountName);
    setChecked(checked);
  }
  
  public void run() {
    // activate this account!
    try {
      CloudBeesUIPlugin.getDefault().setActiveAccountName(accountName);
    } catch (CloudBeesException e) {
      CloudBeesUIPlugin.logErrorAndShowDialog(e);
    }
  }
  
}
