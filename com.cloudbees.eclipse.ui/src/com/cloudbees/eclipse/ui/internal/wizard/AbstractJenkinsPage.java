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

import org.eclipse.jface.wizard.WizardPage;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;

abstract public class AbstractJenkinsPage extends WizardPage {

  protected AbstractJenkinsPage(String pageName) {
    super(pageName);
  }

  protected JenkinsInstance instance;
  private boolean editMode;
  
  public void setJenkinsInstance(JenkinsInstance instance) {
    this.instance = instance;
    editMode = instance.label != null;
  }

  public boolean isEditMode() {
    return editMode;
  }

}
