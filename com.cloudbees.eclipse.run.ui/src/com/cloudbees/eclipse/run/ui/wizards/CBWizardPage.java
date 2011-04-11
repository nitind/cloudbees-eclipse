package com.cloudbees.eclipse.run.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;

public abstract class CBWizardPage extends WizardPage {

  protected CBWizardPage(String pageName) {
    super(pageName);
  }

  abstract boolean canFinish();

  public boolean isActivePage() {
    return isCurrentPage();
  }

}
