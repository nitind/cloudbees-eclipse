package com.cloudbees.eclipse.run.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.cloudbees.eclipse.core.gc.api.ClickStartTemplate;
import com.cloudbees.eclipse.ui.wizard.CBWizardPage;

public class ClickStartTemplateWizardPage extends WizardPage implements CBWizardPage {

  public static final String PAGE_NAME = ClickStartTemplateWizardPage.class.getSimpleName();
  private static final String PAGE_TITLE = "New CloudBees ClickStart Project";
  private static final String PAGE_DESCRIPTION = "Select a template and we will automatically provision services and configure the Eclipse workspace.";

  private ClickStartComposite repositoryComposite;

  protected ClickStartTemplateWizardPage() {
    super(PAGE_NAME);
    setTitle(PAGE_TITLE);
    setDescription(PAGE_DESCRIPTION);
  }

  @Override
  public void createControl(final Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);

    GridLayout layout = new GridLayout(1, true);
    layout.marginHeight = 10;
    layout.marginWidth = 10;
    layout.verticalSpacing = 10;

    container.setLayout(layout);

    GridData data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    data.verticalAlignment = SWT.FILL;

    this.repositoryComposite = new ClickStartComposite(container, getContainer()) {
      @Override
      protected void setPageComplete(boolean b) {
       ClickStartTemplateWizardPage.this.setPageComplete(b);
       System.out.println("Template: "+getTemplate()+" complete? "+b);
      }
    };
    
    this.repositoryComposite.setLayoutData(data);
    
    setPageComplete(false);
    
    setControl(container);
  }

  
  public ClickStartTemplate getTemplate() {
    return this.repositoryComposite.getSelectedTemplate();
  }

  public void updateErrorStatus(final String message) {
    setErrorMessage(message);
    setPageComplete(message == null);
  }

  @Override
  public boolean canFinish() {
    //return false; // Needs project name etc from the next page, can't finish too early.
/*    if (getTemplate()==null) {
      return false;
    }
*/   // return getName()!=null && getName().length()>0;
    return true;
  }

  @Override
  public boolean isActivePage() {
    return isCurrentPage();
  }
  
}
