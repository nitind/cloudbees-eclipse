package com.cloudbees.eclipse.run.ui.wizards;

import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CBProjectNameAndLocationPage extends NewJavaProjectWizardPageOne implements CBWizardPageSupport {

  public static final String PAGE_NAME = CBProjectNameAndLocationPage.class.getSimpleName();
  private static final String PAGE_TITLE = "CloudBees Project";
  private static final String PAGE_DESCRIPTION = "This wizard creates a new CloudBees project.";

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

    //Control jreControl = createJRESelectionControl(composite);
    //jreControl.setLayoutData(new GridData(768));

    //Control layoutControl = createProjectLayoutControl(composite);
    //layoutControl.setLayoutData(new GridData(768));

    Control workingSetControl = createWorkingSetControl(composite);
    workingSetControl.setLayoutData(new GridData(768));

    Control infoControl = createInfoControl(composite);
    infoControl.setLayoutData(new GridData(768));
    infoControl.setVisible(false);

    setControl(composite);
  }

  @Override
  public boolean canFinish() {
    return false;
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
