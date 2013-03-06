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
