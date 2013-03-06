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
      
      @Override
      protected void updateErrorStatus(String msg){
        ClickStartTemplateWizardPage.this.updateErrorStatus(msg);  
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
