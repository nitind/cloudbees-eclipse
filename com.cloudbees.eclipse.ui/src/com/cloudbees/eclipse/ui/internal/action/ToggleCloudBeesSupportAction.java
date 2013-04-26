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
package com.cloudbees.eclipse.ui.internal.action;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesNature;
import com.cloudbees.eclipse.core.NatureUtil;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class ToggleCloudBeesSupportAction implements IObjectActionDelegate {

  public static final String CLOUDBEES_PROJECT_DECORATOR = "com.cloudbees.eclipse.ui.project"; //$NON-NLS-1$

  private ISelection selection;

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  public void run(final IAction action) {
    if (this.selection instanceof IStructuredSelection) {
      for (Iterator<?> it = ((IStructuredSelection) this.selection).iterator(); it.hasNext();) {
        Object element = it.next();
        IProject project = null;
        if (element instanceof IProject) {
          project = (IProject) element;
        } else if (element instanceof IAdaptable) {
          project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
        }
        if (project != null) {
          // TODO: this should use a progress monitor and an operation
          try {
            final NullProgressMonitor monitor = new NullProgressMonitor();
            if (CloudBeesNature.isEnabledFor(project)) {
              NatureUtil.removeNatures(project, CloudBeesCorePlugin.DEFAULT_NATURES, monitor);
              //FacetUtil.uninstallCloudBeesFacet(project, monitor);
            } else {
              NatureUtil.addNatures(project, CloudBeesCorePlugin.DEFAULT_NATURES, monitor);
              //FacetUtil.installCloudBeesFacet(project, monitor);
            }
          } catch (Exception e) {
            CloudBeesUIPlugin.getDefault().getLogger().error(e);
          }
          Display.getDefault().asyncExec(new Runnable() {
            public void run() {
              CloudBeesUIPlugin.getDefault().getWorkbench().getDecoratorManager().update(CLOUDBEES_PROJECT_DECORATOR);
            }
          });
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   * org.eclipse.jface.viewers.ISelection)
   */
  public void selectionChanged(final IAction action, final ISelection selection) {
    this.selection = selection;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.IObjectActionDelegate.setActivePart(org.eclipse.jface.action.IAction,
   * org.eclipse.ui.IWorkbenchPart)
   */
  public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
  }
}
