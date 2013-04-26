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
package com.cloudbees.eclipse.dev.ui.views.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;

import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.util.Utils;
import com.cloudbees.eclipse.dev.core.CloudBeesDevCorePlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.wizard.CBWizardSupport;

public class JenkinsJobWizard extends Wizard {

  private static final String WINDOW_TITLE = "Jenkins Job";
  private static final String ERROR_TITLE = "Error";
  private static final String ERROR_MSG = "Received error while creating new Jenkins job";

  private final IProject project;
  private JenkinsJobWizardPage jenkinsPage;

  public JenkinsJobWizard(final IProject project) {
    this.project = project;
    setNeedsProgressMonitor(true);
    setWindowTitle(WINDOW_TITLE);
    setDefaultPageImageDescriptor(CloudBeesUIPlugin.imageDescriptorFromPlugin(CloudBeesUIPlugin.PLUGIN_ID,
        "icons/cb_wiz_icon.png"));
  }

  @Override
  public void addPages() {
    this.jenkinsPage = new JenkinsJobWizardPage(this.project);
    addPage(this.jenkinsPage);
  }

  @Override
  public boolean performFinish() {
    String jobName = this.jenkinsPage.getJobName();

    try {
      JenkinsInstance instance = this.jenkinsPage.getJenkinsInstance();
      CloudBeesUIPlugin plugin = CloudBeesUIPlugin.getDefault();
      JenkinsService jenkinsService = plugin.lookupJenkinsService(instance);
      CBWizardSupport.makeJenkinsJob(createConfigXML(), jenkinsService, jobName,
          getContainer());

    } catch (Exception e) {
      handleException(e);
    }

    return true;
  }

  private String createConfigXML()
      throws Exception {
    String description = "Builds " + this.project.getName();
    return Utils.createEmptyConfig(description);
  }

  private void handleException(final Exception ex) {
    ex.printStackTrace();
    IStatus status = new Status(IStatus.ERROR, CloudBeesDevCorePlugin.PLUGIN_ID, ex.getMessage(), ex);
    ErrorDialog.openError(getShell(), ERROR_TITLE, ERROR_MSG, status);
  }
}
