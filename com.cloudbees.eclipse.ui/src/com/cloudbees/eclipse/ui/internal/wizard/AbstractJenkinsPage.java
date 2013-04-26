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
