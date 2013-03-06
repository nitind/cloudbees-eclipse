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
