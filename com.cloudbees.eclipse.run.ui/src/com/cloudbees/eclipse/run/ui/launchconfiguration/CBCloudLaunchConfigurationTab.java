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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

public class CBCloudLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  private static final String TAB_NAME = "CloudBees Application";

  protected ProjectSelectionComposite projectSelector;
  protected Composite main;

  private Text customIdText;
  private Button useCustomId;
  private WarSelecionComposite warSelector;

  @Override
  public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
    String projectName = this.projectSelector.getText();
    configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, projectName);

    if (this.useCustomId != null) {
      if (this.useCustomId.getSelection()) {
        configuration
            .setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, this.customIdText.getText());
      } else {
        configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");
      }
    }

    configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_WAR_PATH, this.warSelector.getWarPath());
  }

  @Override
  public void initializeFrom(final ILaunchConfiguration configuration) {
    try {
      String projectName = configuration
          .getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, new String());
      if (projectName == null || projectName.length() == 0) {
        projectName = this.projectSelector.getDefaultSelection();
      }
      this.projectSelector.setText(projectName);

      String id = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_CUSTOM_ID, "");
      this.useCustomId.setSelection(!"".equals(id));
      this.customIdText.setEnabled(!"".equals(id));
      this.customIdText.setText(id);

      String warPath = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_WAR_PATH, "");
      this.warSelector.setWarPath(warPath);

      updateLaunchConfigurationDialog();
    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
    scheduleUpdateJob();
  }

  /**
   * @wbp.parser.entryPoint
   */
  @Override
  public void createControl(final Composite parent) {
    this.main = new Composite(parent, SWT.NONE);
    this.main.setLayout(new GridLayout(2, false));

    this.projectSelector = new ProjectSelectionComposite(this.main, SWT.None) {
      @Override
      public void handleUpdate() {
        IProject proj = null;
        String projectName = CBCloudLaunchConfigurationTab.this.projectSelector.getText();
        if (projectName != null && !projectName.isEmpty()) {
          for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            if (project.getName().equals(projectName)) {
              proj = project;
              break;
            }
          }
          CBCloudLaunchConfigurationTab.this.warSelector.setUseCustomWar(!hasBuildXml(projectName));
        }
        CBCloudLaunchConfigurationTab.this.warSelector.setProject(proj);

        validateConfigurationTab();
      }
    };
    this.projectSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    setControl(this.main);

    this.warSelector = new WarSelecionComposite(this.main) {
      @Override
      public void handleUpdate() {
        validateConfigurationTab();
      }
    };
    this.warSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    Composite customIdComposite = new Composite(this.main, SWT.NONE);
    customIdComposite.setLayout(new GridLayout());
    customIdComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    Group customIdGroup = new Group(customIdComposite, SWT.NONE);
    customIdGroup.setText("App ID");
    customIdGroup.setLayout(new GridLayout(1, false));
    customIdGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

    this.useCustomId = new Button(customIdGroup, SWT.CHECK);
    this.useCustomId.setSelection(false);
    this.useCustomId.setText("Use Custom App ID");
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalSpan = 2;
    this.useCustomId.setLayoutData(gridData);

    //    final Label label = new Label(this.main, SWT.NONE);
    //    label.setEnabled(false);
    //    label.setText("App ID");

    this.customIdText = new Text(customIdGroup, SWT.SINGLE | SWT.BORDER);
    this.customIdText.setText("");
    this.customIdText.setEnabled(false);
    this.customIdText.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(final ModifyEvent e) {
        updateLaunchConfigurationDialog();
      }
    });
    gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = SWT.FILL;
    this.customIdText.setLayoutData(gridData);

    this.useCustomId.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        boolean selection = CBCloudLaunchConfigurationTab.this.useCustomId.getSelection();
        //        label.setEnabled(selection);
        CBCloudLaunchConfigurationTab.this.customIdText.setEnabled(selection);
        updateLaunchConfigurationDialog();
      }

      @Override
      public void widgetDefaultSelected(final SelectionEvent e) {
        widgetSelected(e);
      }
    });
  }

  protected boolean validateConfigurationTab() {
    IStatus projectStatus = this.projectSelector.validate();
    if (!projectStatus.isOK()) {
      setErrorMessage(projectStatus.getMessage());
      updateLaunchConfigurationDialog();
      return false;
    }

    IStatus warStatus = this.warSelector.validate();
    if (!warStatus.isOK()) {
      setErrorMessage(warStatus.getMessage());
      updateLaunchConfigurationDialog();
      return false;
    }

    setErrorMessage(null);
    setMessage("Run CloudBees application");
    updateLaunchConfigurationDialog();

    return true;
  }

  public static boolean hasBuildXml(final String projectName) {
    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(projectName + "/build.xml"));
    boolean projectWithAnt = file.exists();
    return projectWithAnt;
  }

  @Override
  public boolean isValid(final ILaunchConfiguration launchConfig) {
    return this.projectSelector.validate().getSeverity() == IStatus.OK;
  }

  @Override
  public String getName() {
    return TAB_NAME;
  }

  @Override
  public Image getImage() {
    return CBRunUiActivator.getDefault().getImageRegistry().get(Images.CLOUDBEES_ICON_16x16);
  }

  @Override
  public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
  }
}
