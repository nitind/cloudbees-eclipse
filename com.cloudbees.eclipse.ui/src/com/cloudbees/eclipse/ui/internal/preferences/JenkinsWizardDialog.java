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
package com.cloudbees.eclipse.ui.internal.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.internal.wizard.JenkinsFinishPage;
import com.cloudbees.eclipse.ui.internal.wizard.JenkinsWizard;

public class JenkinsWizardDialog extends WizardDialog {

  public JenkinsWizardDialog(final Shell parent) {
    super(parent, new JenkinsWizard());
  }

  public JenkinsWizardDialog(final Shell parent, final JenkinsInstance ni) {
    super(parent, new JenkinsWizard(ni));
  }

  @Override
  protected void nextPressed() {
    if ("url".equals(getCurrentPage().getName())) {
      try {
        run(false, true, new IRunnableWithProgress() {

          public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.setTaskName("Validating Jenkins URL...");

            JenkinsService ns = CloudBeesUIPlugin.getDefault().lookupJenkinsService(
                ((JenkinsWizard) getWizard()).getJenkinsInstance());

            try {
              JenkinsInstanceResponse instance = ns.getInstance(monitor);
              if (instance == null) {
                throw new CloudBeesException("Failed to check instance: " + ns.getUrl());
              }

              ((JenkinsFinishPage) ((JenkinsWizard) getWizard()).getPage("finish")).initText(null);
            } catch (CloudBeesException e) {
              ((JenkinsFinishPage) ((JenkinsWizard) getWizard()).getPage("finish")).initText(e);

              CloudBeesUIPlugin.getDefault().getLogger().error(e);
            }

            monitor.done();
          }

        });
      } catch (InvocationTargetException e) {
        CloudBeesUIPlugin.getDefault().getLogger().error(e);
      } catch (InterruptedException e) {
        CloudBeesUIPlugin.getDefault().getLogger().error(e);
      }

    }
    super.nextPressed();
  }

}
