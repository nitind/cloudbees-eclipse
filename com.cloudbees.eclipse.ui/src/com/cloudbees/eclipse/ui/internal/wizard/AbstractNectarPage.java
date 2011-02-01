package com.cloudbees.eclipse.ui.internal.wizard;

import org.eclipse.jface.wizard.WizardPage;

import com.cloudbees.eclipse.core.domain.NectarInstance;

abstract public class AbstractNectarPage extends WizardPage {

  protected AbstractNectarPage(String pageName) {
    super(pageName);
  }

  protected NectarInstance ni;
  
  
  public void setNectarInstance(NectarInstance ni) {
    this.ni = ni;
  }

}
