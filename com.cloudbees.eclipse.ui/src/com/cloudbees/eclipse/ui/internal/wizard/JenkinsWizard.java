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
package com.cloudbees.eclipse.ui.internal.wizard;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class JenkinsWizard extends Wizard {

  private final JenkinsInstance instance;

  private final JenkinsUrlPage pageUrl;
  private final JenkinsFinishPage pageFinish;

  public JenkinsWizard() {
    this(new JenkinsInstance());
  }

  public JenkinsWizard(final JenkinsInstance ni) {
    this.instance = ni;

    setNeedsProgressMonitor(true);
    ImageDescriptor id = ImageDescriptor.createFromURL(CloudBeesUIPlugin.getDefault().getBundle()
        .getResource("/icons/cb_wiz_icon2.png"));
    setDefaultPageImageDescriptor(id);
    if (ni.label == null) {
      setWindowTitle("New Jenkins instance");
    } else {
      setWindowTitle("Edit Jenkins instance");
    }
    setForcePreviousAndNextButtons(true);
    setHelpAvailable(false);

    this.pageUrl = new JenkinsUrlPage(ni);
    this.pageFinish = new JenkinsFinishPage(ni);
  }

  @Override
  public void addPages() {
    addPage(this.pageUrl);
    addPage(this.pageFinish);
  }

  @Override
  public boolean performFinish() {
    saveInstanceInfo();
    return true;
  }

  public JenkinsInstance getJenkinsInstance() {
    return this.instance;
  }

  private void saveInstanceInfo() {

    // TODO shouldn't we actually save only when Apply clicked on the preference page?
    CloudBeesUIPlugin.getDefault().saveJenkinsInstance(this.instance);

  }

}
