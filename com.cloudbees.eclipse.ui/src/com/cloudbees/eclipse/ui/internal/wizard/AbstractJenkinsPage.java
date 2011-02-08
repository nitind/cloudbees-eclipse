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
