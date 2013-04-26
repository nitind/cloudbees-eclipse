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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.eclipse.dev.ui.views.wizards.JenkinsJobWizard;

public class NewJenkinsJobAction implements IObjectActionDelegate {

  public NewJenkinsJobAction() {
  }

  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {

      IProject project = (IProject) ((StructuredSelection) ((ObjectPluginAction) action).getSelection())
          .getFirstElement();

      Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
      WizardDialog dialog = new WizardDialog(shell, new JenkinsJobWizard(project));
      dialog.create();
      dialog.open();
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

}
