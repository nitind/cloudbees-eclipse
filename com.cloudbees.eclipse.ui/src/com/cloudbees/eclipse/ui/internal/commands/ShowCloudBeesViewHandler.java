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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class ShowCloudBeesViewHandler extends AbstractHandler {

  public Object execute(ExecutionEvent event) throws ExecutionException {
    try {
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
          .showView("com.cloudbees.eclipse.ui.views.CBTreeView");
    } catch (PartInitException e) {
      CloudBeesUIPlugin.getDefault().getLogger().error(e);
      Shell shell = Display.getDefault().getActiveShell();
      Status status = new Status(IStatus.ERROR, CloudBeesUIPlugin.PLUGIN_ID, e.getMessage(), e);
      ErrorDialog.openError(shell, "Error", "Can't open CloudBees view", status);
    }
    return null;
  }

}
