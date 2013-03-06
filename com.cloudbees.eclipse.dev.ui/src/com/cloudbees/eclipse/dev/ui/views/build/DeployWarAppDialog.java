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
package com.cloudbees.eclipse.dev.ui.views.build;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Artifact;

public class DeployWarAppDialog extends Dialog {

  protected Combo comboWar;
  protected Combo comboApp;

  protected List<Artifact> wars;
  protected List<ApplicationInfo> apps;

  protected Artifact selectedWar;
  protected ApplicationInfo selectedApp;

  /**
   * Create the dialog.
   * @param parentShell
   */
  public DeployWarAppDialog(final Shell parentShell, final List<Artifact> wars, final Artifact selectedWar,
      final List<ApplicationInfo> apps) {
    super(parentShell);
    this.wars = wars;
    this.apps = apps;
    this.selectedWar = selectedWar;
  }

  /**
   * Create contents of the dialog.
   * @param parent
   */
  @Override
  protected Control createDialogArea(final Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    GridLayout gridLayout = (GridLayout) container.getLayout();

    Label labelIntro = new Label(container, SWT.NONE);
    labelIntro.setText("Select war and RUN@cloud application to deploy war to.");
    new Label(container, SWT.NONE);

    Label labelWar = new Label(container, SWT.NONE);
    labelWar.setText("Available war artifacts:");

    this.comboWar = new Combo(container, SWT.NONE);
    this.comboWar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    new Label(container, SWT.NONE);

    Label labelApp = new Label(container, SWT.NONE);
    labelApp.setText("Available RUN@cloud applications:");

    this.comboApp = new Combo(container, SWT.NONE);
    this.comboApp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    initCombos();

    return container;
  }

  public void initCombos() {
    int tally = 0;
    for (Artifact war : this.wars) {
      this.comboWar.add(war.displayPath);
      if (tally == 0 || war == this.selectedWar) {
        this.comboWar.select(tally);
      }
      ++tally;
    }

    tally = 0;
    for (ApplicationInfo app : this.apps) {
      this.comboApp.add(app.getId());
      if (tally == 0) {
        this.comboApp.select(tally);
      }
    }
  }

  /**
   * Create contents of the button bar.
   * @param parent
   */
  @Override
  protected void createButtonsForButtonBar(final Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
  }

  /**
   * Return the initial size of the dialog.
   */
  @Override
  protected Point getInitialSize() {
    return new Point(450, 300);
  }

  @Override
  protected void okPressed() {
    this.selectedWar = this.wars.get(this.comboWar.getSelectionIndex());
    this.selectedApp = this.apps.get(this.comboApp.getSelectionIndex());
    super.okPressed();
  }

  @Override
  protected void cancelPressed() {
    this.selectedWar = null;
    this.selectedApp = null;
    super.cancelPressed();
  }

  public Artifact getSelectedWar() {
    return this.selectedWar;
  }

  public ApplicationInfo getSelectedApp() {
    return this.selectedApp;
  }
}
