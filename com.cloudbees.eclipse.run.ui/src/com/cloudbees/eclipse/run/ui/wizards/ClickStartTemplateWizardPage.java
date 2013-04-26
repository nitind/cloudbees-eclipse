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
package com.cloudbees.eclipse.run.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

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
    
    GridLayout pgl = new GridLayout(1, false);
    //parent.setLayout(pgl);  
    GridData pgd = new GridData(SWT.FILL, SWT.FILL);
    pgd.heightHint=500;
    pgd.widthHint=800;
    pgd.grabExcessHorizontalSpace=true;
    pgd.grabExcessVerticalSpace=true;
    pgd.horizontalAlignment=SWT.FILL;
    pgd.verticalAlignment=SWT.FILL;
    parent.setLayoutData(pgd);
    
    Composite container = new Composite(parent, SWT.NONE);

    GridLayout layout = new GridLayout(1, true);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.verticalSpacing = 0;

    container.setLayout(layout);
    GridData cgd = new GridData(SWT.FILL, SWT.FILL);
    cgd.grabExcessHorizontalSpace=true;
    cgd.grabExcessVerticalSpace=true;
    cgd.horizontalAlignment = SWT.FILL;
    cgd.verticalAlignment = SWT.FILL;
    container.setLayoutData(cgd);

/*    GridData data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    data.verticalAlignment = SWT.FILL;
*/
    this.repositoryComposite = new ClickStartComposite(container, getContainer()) {
      @Override
      protected void setPageComplete(boolean b) {
       ClickStartTemplateWizardPage.this.setPageComplete(b);
       //System.out.println("Template: "+getTemplate()+" complete? "+b);
      }
      
      @Override
      protected void updateErrorStatus(String msg){
        ClickStartTemplateWizardPage.this.updateErrorStatus(msg);  
      }
      
    };
    
    //repositoryComposite.setLayout(new GridLayout(1, false));
    //this.repositoryComposite.setLayoutData(data);
    
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
