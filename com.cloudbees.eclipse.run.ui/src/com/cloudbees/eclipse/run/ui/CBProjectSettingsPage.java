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
package com.cloudbees.eclipse.run.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;

public class CBProjectSettingsPage extends PropertyPage {

  private Text textAppId;

  private IProject project;

  @Override
  protected Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout();
    container.setLayout(layout);
    container.setLayoutData(new GridData(GridData.FILL_BOTH));

    GridLayout gridLayout = (GridLayout) container.getLayout();
    gridLayout.numColumns = 2;
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);

    Label lblAppId = new Label(container, SWT.NONE);
    lblAppId.setText("Project-based App ID:");
    new Label(container, SWT.NONE);

    this.textAppId = new Text(container, SWT.BORDER);
    this.textAppId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    IProject p = null;
    final IAdaptable element = getElement();
    if (element instanceof IResource) {
      p = ((IResource) element).getProject();
    } else {
      Object adapter = element.getAdapter(IResource.class);
      if (adapter instanceof IResource) {
        p = ((IResource) adapter).getProject();
      }
    }

    if (p != null) {

      String appIdName = p.getName();

      String account = null;

      try {
        String val = p.getPersistentProperty(CloudBeesCorePlugin.PRJ_APPID_KEY);
        if (val != null && val.length() > 0) {
          appIdName = val;
        }

        String val2 = p.getPersistentProperty(CloudBeesCorePlugin.PRJ_ACCOUNT_KEY);
        if (val2 != null && val2.length() > 0) {
          account = val2;
        }

      } catch (CoreException e) {
        e.printStackTrace();
      }
      textAppId.setText(appIdName);

      if (account != null && account.length() > 0) {
        new Label(container, SWT.NONE);
        Label lblAccount = new Label(container, SWT.NONE);        
        lblAccount.setText("ClickStart-based account name: "+account);
      }
      
      setMessage("CloudBees settings for '" + p.getName() + "'");

      this.project = p;
    }
    //setDescription("descr test");
    //setMessage("Test message");
    //setTitle("Title test");

    return container;
  }

  @Override
  public boolean okToLeave() {
    boolean empty = textAppId.getText() == null || textAppId.getText().length() == 0;
    if (empty) {
      setErrorMessage("App ID can't be set to empty.");
    }
    return super.okToLeave() && !empty;
  }

  @Override
  public boolean performOk() {
    // store app id
    String newId = textAppId.getText();
    try {
      project.setPersistentProperty(CloudBeesCorePlugin.PRJ_APPID_KEY, newId);
    } catch (CoreException e) {
      setErrorMessage(e.getMessage());
      e.printStackTrace();
      return false;
    }

    return super.performOk();
  }
}
