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
package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.core.util.CBRunUtil;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

public class CBLocalLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

  private static final String TAB_NAME = "CloudBees Application";

  protected ProjectSelectionComposite projectSelector;
  protected Composite main;

  private PortSelectionComposite portSelectionComposite;
  private PortSelectionComposite debugPortSelectionComposite;

  private DeployArtifactSelecionComposite warSelector;

  /**
   * @wbp.parser.entryPoint
   */
  @Override
  public void createControl(Composite parent) {
    this.main = new Composite(parent, SWT.NONE);
    this.main.setLayout(new GridLayout(2, false));

    this.projectSelector = new ProjectSelectionComposite(this.main, SWT.None) {
      @Override
      public void handleUpdate() {
        
        warSelector.setUseCustomFile(false);
        warSelector.setFilePath("");
        
        String projectName = CBLocalLaunchConfigurationTab.this.projectSelector.getText();
        if (projectName != null && !projectName.isEmpty()) {
          IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
          if (project!=null && project.isOpen()) {
            warSelector.setProject(project);
          }
        }
        
        validateConfigurationTab();
      }
    };
    this.projectSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    this.warSelector = new DeployArtifactSelecionComposite(this.main) {
      @Override
      public void handleUpdate() {
        IProject proj = null;
        String projectName = CBLocalLaunchConfigurationTab.this.projectSelector.getText();
        String warPath = null;
        if (CBLocalLaunchConfigurationTab.this.warSelector != null) {
          warPath = CBLocalLaunchConfigurationTab.this.warSelector.getFilePath();
        }

        if (projectName != null && !projectName.isEmpty()) {
          for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
            if (project.getName().equals(projectName)) {
              proj = project;
              break;
            }
          }

          //CBLocalLaunchConfigurationTab.this.warSelector.setUseCustomWar(warPath != null && warPath.length() > 0);

        }

        if (proj != null) {
          CBLocalLaunchConfigurationTab.this.warSelector.setProject(proj);
        }

        updateLaunchConfigurationDialog();
        validateConfigurationTab();
      }
    };
    this.warSelector.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    this.portSelectionComposite = new PortSelectionComposite(this.main, SWT.None) {
      @Override
      public void handleChange() {
        try {
          int port = Integer.parseInt(getPort());
          if (port < 1) {
            setErrorMessage("Port must be bigger than 0");
          } else if (port > 65535) {
            setErrorMessage("Port must be smaller than 35535");
          } else {
            setErrorMessage(null);
          }
        } catch (NumberFormatException e) {
          setErrorMessage("Port number must be an integer!");
        }

        updateLaunchConfigurationDialog();
        validateConfigurationTab();
      }

      public String getDefaultPort() {
        return CBRunUtil.getDefaultLocalPort() + "";
      }

      public String getPortLabel() {
        return "HTTP Port";
      }
    };
    this.portSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    this.debugPortSelectionComposite = new PortSelectionComposite(this.main, SWT.None) {
      @Override
      public void handleChange() {
        try {
          int port = Integer.parseInt(getPort());
          if (port < 1) {
            setErrorMessage("Port must be bigger than 0");
          } else if (port > 65535) {
            setErrorMessage("Port must be smaller than 35535");
          } else {
            setErrorMessage(null);
          }
        } catch (NumberFormatException e) {
          setErrorMessage("Port number must be an integer!");
        }
        updateLaunchConfigurationDialog();
      }

      public String getDefaultPort() {
        return CBRunUtil.getDefaultLocalDebugPort() + "";
      }

      public String getPortLabel() {
        return "Debug Port";
      }

    };
    this.debugPortSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

    setControl(this.main);

  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
  }

  @Override
  public void initializeFrom(ILaunchConfiguration configuration) {
    try {

      String warPath = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_WAR_PATH, "");
      warSelector.setUseCustomFile(warPath != null && warPath.length() > 0);
      this.warSelector.setFilePath(warPath);
      
      String projectName = configuration
          .getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PROJECT_NAME, new String());
      if (projectName == null || projectName.length() == 0) {
        projectName = this.projectSelector.getDefaultSelection();
      }
      this.projectSelector.setText(projectName);
      
      IProject project = null;
      if (projectName != null & projectName.length() > 0) {
        project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      }
      
      if (project!=null) {
        warSelector.setProject(project);
      }

      String port = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_PORT, new String());
      this.portSelectionComposite.setPort(port);

      String dport = configuration.getAttribute(CBLaunchConfigurationConstants.ATTR_CB_DEBUG_PORT, new String());
      this.debugPortSelectionComposite.setPort(dport);

    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
    scheduleUpdateJob();
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {

    String projectName = this.projectSelector.getText();

    String warPath = this.warSelector.getFilePath();

    //configuration.setAttribute(CBLaunchConfigurationConstants.ATTR_CB_LAUNCH_WAR_PATH, warPath);

    try {

      IProject project = null;
      if (projectName != null & projectName.length() > 0) {
        project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
      }

      IResource res = null;
      if (project != null && warPath != null && warPath.length() > 0) {
        res = project.getFile(warPath);
      }
      if (res == null) {
        res = project;
      }

      CBRunUtil.addLaunchConfLocalAttributes(configuration, res, this.portSelectionComposite.getPort(),
          this.debugPortSelectionComposite.getPort());

    } catch (CoreException e) {
      CBRunUiActivator.logError(e);
    }
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

  @Override
  public String getName() {
    return TAB_NAME;
  }

  @Override
  public Image getImage() {
    return CBRunUiActivator.getDefault().getImageRegistry().get(Images.CLOUDBEES_ICON_16x16);
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {
    return this.projectSelector.validate().getSeverity() == IStatus.OK && getErrorMessage() == null;
  }

}
