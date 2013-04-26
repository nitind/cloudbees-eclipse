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
package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.dev.ui.CBDEVImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class ReloadJobsAction extends Action {

  public String viewUrl;
  public TreeViewer treeViewer;

  public ReloadJobsAction() {
    super();

    setText("Reload Jenkins jobs");
    setToolTipText("Reload Jenkins jobs");
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBDEVImages.IMG_REFRESH));

    setAccelerator(SWT.F5);

  }

  @Override
  public void runWithEvent(final Event event) {

    try {
      
      CloudBeesDevUiPlugin.getDefault().showJobs(this.viewUrl, true); 
    } catch (CloudBeesException e) {
      CloudBeesUIPlugin.showError("Failed to reload Jenkins jobs!", e);
    }

  }

}
