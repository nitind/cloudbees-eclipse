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
package com.cloudbees.eclipse.dev.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.dev.ui.CBDEVImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;

/**
 * @author antons
 */
public class OpenLogAction extends Action {

  private Object build;

  public OpenLogAction() {
    super("Open console log", Action.AS_PUSH_BUTTON | SWT.NO_FOCUS); //TODO i18n
    setToolTipText("Open console log"); //TODO i18n
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBDEVImages.IMG_CONSOLE));
    super.setEnabled(false);
  }

  @Override
  public void setEnabled(final boolean enable) {
    // ignore
  }

  public void setBuild(final Object build) {
    this.build = build;
    super.setEnabled(this.build != null);
  }

  @Override
  public boolean isEnabled() {
    return this.build != null;
  }

  @Override
  public void run() {
    if (this.build instanceof JenkinsBuild) {
      CloudBeesDevUiPlugin.getDefault().getJobConsoleManager()
          .showConsole(((JenkinsBuild) this.build).getDisplayName(), ((JenkinsBuild) this.build).url);
    } else if (this.build instanceof JenkinsBuildDetailsResponse) {
      CloudBeesDevUiPlugin.getDefault().getJobConsoleManager()
          .showConsole(((JenkinsBuildDetailsResponse) this.build).getDisplayName(),
              ((JenkinsBuildDetailsResponse) this.build).url);
    }
  }
}
