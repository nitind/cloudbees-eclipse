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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.cloudbees.eclipse.ui.CBImages;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.UIUtils;

public class AccountSelectionDialog extends TitleAreaDialog {

  private static final String TITLE = "CloudBees Account Selection";
  private static final String DESCRIPTION = "Multiple accounts detected.\nLater you can switch using toolbar pull-down menu.";
  private static final String ERROR_TITLE = "Error";
  private static final Image ICON = CloudBeesUIPlugin.getImage(CBImages.ICON_CB_WIZARD);

  private final String[] accountNames;
  private String selectedAccountName;

  public AccountSelectionDialog(Shell shell, String[] accountNames) {
    super(shell);
    this.accountNames = accountNames;
  }

  @Override
  protected Control createContents(Composite parent) {
    Control contents = super.createContents(parent);
    setTitle(TITLE);
    setMessage(DESCRIPTION);
    setTitleImage(ICON);
    getShell().setSize(400, 400);
    getShell().setMinimumSize(400, 400);
    getShell().setText(TITLE);

    Point shellCenter = UIUtils.getCenterPoint();
    getShell().setLocation(shellCenter.x - 400 / 2, shellCenter.y - 400 / 2);
    
    
    if (this.accountNames == null || this.accountNames.length == 0) {
      getButton(OK).setEnabled(false);
    }

    return contents;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);

    Composite content = new Composite(area, SWT.NONE);
    content.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 10;
    gridLayout.marginHeight = 10;
    content.setLayout(gridLayout);

    Label label = new Label(content, SWT.NONE);
    label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
    label.setText("Please select the account to use:");

    final List list = new org.eclipse.swt.widgets.List(content, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL);
    list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    list.addSelectionListener(new SelectionListener() {

      public void widgetSelected(SelectionEvent e) {
        handleSelectionChange(list);
      }

      public void widgetDefaultSelected(SelectionEvent e) {
        handleSelectionChange(list);
      }
    });
    list.setItems(this.accountNames);

    return area;
  }

  private void handleException(String msg, Throwable t) {
    Status status = new Status(IStatus.ERROR, CloudBeesUIPlugin.PLUGIN_ID, msg, t);
    handleException(msg, status);
  }

  private void handleException(String msg, IStatus status) {
    CloudBeesUIPlugin.logError(status.getException());
    ErrorDialog.openError(getShell(), ERROR_TITLE, msg, status);
  }

  @Override
  protected boolean isResizable() {
    return true;
  }

  @Override
  public boolean isHelpAvailable() {
    return false;
  }

  private void handleSelectionChange(List list) {
    if (list.getSelectionCount() == 0) {
      this.selectedAccountName = null;
    }
    if (list.getSelection().length>0) {
      this.selectedAccountName = list.getSelection()[0];
    }
  }

  public String getSelectedAccountName() {
    return this.selectedAccountName;
  }

}
