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
package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CustomAppIdDialog extends Dialog {
  private String projectName;
  private Text textAppId;
  private String appId = null;

  /**
   * Create the dialog.
   * @param parentShell
   */
  public CustomAppIdDialog(final Shell parentShell, final String projectName) {
    super(parentShell);
    this.projectName = projectName;
  }

  /**
   * Create contents of the dialog.
   * @param parent
   */
  @Override
  protected Control createDialogArea(final Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout gridLayout = (GridLayout) container.getLayout();
    gridLayout.numColumns = 2;
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);
    new Label(container, SWT.NONE);

    Label lblAppId = new Label(container, SWT.NONE);
    lblAppId.setText("Specify custom App ID for war deploy:");
    new Label(container, SWT.NONE);

    this.textAppId = new Text(container, SWT.BORDER);
    this.textAppId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    this.textAppId.setText(this.projectName);

    return container;
  }

  @Override
  protected void configureShell(final Shell newShell) {
    newShell.setText("Custom App ID");
    super.configureShell(newShell);
  }

  /**
   * Return the initial size of the dialog.
   */
  @Override
  protected Point getInitialSize() {
    return new Point(450, 208);
  }

  @Override
  protected void okPressed() {
    this.appId = this.textAppId.getText();
    super.okPressed();
  }

  public String getAppId() {
    return this.appId;
  }
}
