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

public class ReferenceHandler extends AbstractHandler {

  public Object execute(ExecutionEvent event) throws ExecutionException {
    // By default show CloudBees View
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
