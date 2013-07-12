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
package com.cloudbees.eclipse.ui.internal;

import org.eclipse.jface.action.Action;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class SelectRegionAction extends Action {

  private String regionName;

  public SelectRegionAction(String regionName, boolean checked) {
    super();
    this.regionName = regionName;
    
    setText(regionName);
    setChecked(checked);
  }
  
  public void run() {
    // activate this region
    try {
      CloudBeesUIPlugin.getDefault().setActiveRegion(regionName);
    } catch (CloudBeesException e) {
      CloudBeesUIPlugin.logErrorAndShowDialog(e);
    }
  }
  
}
