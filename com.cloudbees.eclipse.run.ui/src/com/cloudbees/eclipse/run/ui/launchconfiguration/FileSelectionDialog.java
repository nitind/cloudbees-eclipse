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

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

public class FileSelectionDialog extends TitleAreaDialog {

  private static final String TITLE = "War File Selection";
  private static final String DESCRIPTION = "Please select the war file you want to deploy";
  private static final Image ICON = CBRunUiActivator.getImage(Images.CLOUDBEES_WIZ_ICON);

  private final String[] warPaths;
  private String selectedWarPath;

  public FileSelectionDialog(final Shell shell, final String[] warPaths) {
    super(shell);
    this.warPaths = warPaths;
  }

  @Override
  protected Control createContents(final Composite parent) {
    Control contents = super.createContents(parent);
    setTitle(TITLE);
    setMessage(DESCRIPTION);
    setTitleImage(ICON);
    getShell().setSize(400, 400);
    getShell().setMinimumSize(250, 250);
    return contents;
  }

  @Override
  protected Control createDialogArea(final Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);

    Composite content = new Composite(area, SWT.NONE);
    content.setLayoutData(new GridData(GridData.FILL_BOTH));
    GridLayout gridLayout = new GridLayout();
    gridLayout.marginWidth = 10;
    gridLayout.marginHeight = 10;
    content.setLayout(gridLayout);

    Label label = new Label(content, SWT.NONE);
    label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
    label.setText("Select war file:");

    final List list = new org.eclipse.swt.widgets.List(content, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL);
    list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    list.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        handleSelectionChange(list);
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
        handleSelectionChange(list);
      }
    });
    list.setItems(this.warPaths);

    return area;
  }

  @Override
  protected boolean isResizable() {
    return true;
  }

  @Override
  public boolean isHelpAvailable() {
    return false;
  }

  private void handleSelectionChange(final List list) {
    if (list.getSelectionCount() == 0) {
      this.selectedWarPath = null;
    }
    this.selectedWarPath = list.getSelection()[0];
  }

  public String getSelectedFilePath() {
    return this.selectedWarPath;
  }
}
