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

import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.cloudbees.eclipse.ui.wizard.CBWizardPage;

public class CBProjectNameAndLocationPage extends NewJavaProjectWizardPageOne implements CBWizardPage {

  public static final String PAGE_NAME = CBProjectNameAndLocationPage.class.getSimpleName();
  private static final String PAGE_TITLE = "CloudBees project name and location";
  private static final String PAGE_DESCRIPTION = "Please set the name and location for the project.";

  public CBProjectNameAndLocationPage() {
    super();
    setTitle(PAGE_TITLE);
    setDescription(PAGE_DESCRIPTION);
  }

  @Override
  public void createControl(Composite parent) {
    initializeDialogUnits(parent);

    Composite composite = new Composite(parent, 0);
    composite.setFont(parent.getFont());
    composite.setLayout(initGridLayout(new GridLayout(1, false), true));
    composite.setLayoutData(new GridData(256));

    Control nameControl = createNameControl(composite);
    nameControl.setLayoutData(new GridData(768));

    Control locationControl = createLocationControl(composite);
    locationControl.setLayoutData(new GridData(768));

    Control workingSetControl = createWorkingSetControl(composite);
    workingSetControl.setLayoutData(new GridData(768));

    Control infoControl = createInfoControl(composite);
    infoControl.setLayoutData(new GridData(768));
    infoControl.setVisible(false);

    setControl(composite);
  }

  @Override
  public boolean canFinish() {    
    return true;
  }

  @Override
  public boolean isActivePage() {
    return isCurrentPage();
  }

  protected GridLayout initGridLayout(GridLayout layout, boolean margins) {
    layout.horizontalSpacing = convertHorizontalDLUsToPixels(4);
    layout.verticalSpacing = convertVerticalDLUsToPixels(4);
    if (margins) {
      layout.marginWidth = convertHorizontalDLUsToPixels(7);
      layout.marginHeight = convertVerticalDLUsToPixels(7);
    } else {
      layout.marginWidth = 0;
      layout.marginHeight = 0;
    }
    return layout;
  }
}
