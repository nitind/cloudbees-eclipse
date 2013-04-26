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
package com.cloudbees.eclipse.dtp.internal.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

@SuppressWarnings("restriction")
public class ShowPasswordAction implements IObjectActionDelegate {

  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {
      ObjectPluginAction pluginAction = (ObjectPluginAction) action;
      ISelection selection = pluginAction.getSelection();

      if (selection instanceof IStructuredSelection) {
        IStructuredSelection structSelection = (IStructuredSelection) selection;
        Object element = structSelection.getFirstElement();

        if (element instanceof DatabaseInfo) {
          DatabaseInfo db = (DatabaseInfo) element;
          try {
            final DatabaseInfo dbi = BeesSDK.getDatabaseInfo(db.getName(), true);
            final String psw = dbi.getPassword();
            
            Dialog pswd = new Dialog(Display.getCurrent().getActiveShell()) {

              @Override
              protected void configureShell(Shell newShell) {
                super.configureShell(newShell);
                newShell.setText("Password for database '"+dbi.getName()+"'");
              }
              
              protected void createButtonsForButtonBar(Composite parent) {
                createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                    true);
              }
              
              @Override
              protected Control createDialogArea(Composite parent) {
                Composite container = (Composite) super.createDialogArea(parent);
                GridLayout layout = new GridLayout();
                layout.numColumns = 2;                
                container.setLayout(layout);
                GridData gridData = new GridData();
                gridData.grabExcessHorizontalSpace = true;
                gridData.horizontalAlignment = GridData.FILL;

                Label dbLabel = new Label(container, SWT.NONE);
                dbLabel.setText("Database: ");

                Text dbNameText = new Text(container, SWT.BORDER);
                dbNameText.setText(dbi.getName());                
                dbNameText.setLayoutData(gridData);
                dbNameText.setEditable(false);
                dbNameText.setEnabled(true);
                
                Label pswLabel = new Label(container, SWT.NONE);
                pswLabel.setText("Password: ");
                gridData = new GridData();
                gridData.grabExcessHorizontalSpace = true;
                gridData.horizontalAlignment = GridData.FILL;
                Text pswText = new Text(container, SWT.BORDER);
                pswText.setText(psw);
                pswText.setEditable(false);
                pswText.setEnabled(true);
                pswText.setLayoutData(gridData);

                pswText.setFocus();
                pswText.selectAll();
                return container;
              }
            };
            pswd.create();
            pswd.open(); 
            
            
            //MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Password for database '"+dbi.getName()+"'", "Database: "+dbi.getName()+"\nPassword: "+dbi.getPassword());
          } catch (Exception e) {
            CloudBeesUIPlugin.showError("Failed to fetch database password!", e);
          }
          
        }
      }
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

}
