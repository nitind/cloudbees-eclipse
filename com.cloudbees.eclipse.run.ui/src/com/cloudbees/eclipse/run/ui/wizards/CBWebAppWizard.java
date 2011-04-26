package com.cloudbees.eclipse.run.ui.wizards;

import java.text.MessageFormat;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.cloudbees.eclipse.dev.core.CloudBeesDevCorePlugin;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;
import com.cloudbees.eclipse.ui.wizard.CBWizardPage;

public class CBWebAppWizard extends BasicNewResourceWizard implements INewWizard {

  private static final String WINDOW_TITLE = "CloudBees Project";
  private static final String BUILD_LABEL = "Build {0}";

  private CBProjectNameAndLocationPage nameAndLocPage;
  private CBServicesWizardPage servicesPage;

  public CBWebAppWizard() {
    super();
    setNeedsProgressMonitor(true);
    setWindowTitle(WINDOW_TITLE);
    CloudBeesDevCorePlugin.getDefault(); // initialize forge providers
  }

  @Override
  public void addPages() {
    this.nameAndLocPage = new CBProjectNameAndLocationPage();
    addPage(this.nameAndLocPage);

    this.servicesPage = new CBServicesWizardPage();
    addPage(this.servicesPage);

    this.nameAndLocPage.init(getSelection(), getActivePart());
  }

  @Override
  public IWizardPage getNextPage(IWizardPage page) {
    if (page instanceof CBServicesWizardPage) {
      CBServicesWizardPage jenkinsPage = (CBServicesWizardPage) page;
      String jobName = jenkinsPage.getJobNameText();

      if (jobName == null || jobName.length() == 0) {
        jenkinsPage.setJobNameText(MessageFormat.format(BUILD_LABEL, this.nameAndLocPage.getProjectName()));
      }

    }

    return super.getNextPage(page);
  }

  @Override
  public boolean canFinish() {
    for (IWizardPage page : getPages()) {
      if (!(page instanceof CBWizardPage)) {
        continue;
      }

      CBWizardPage p = (CBWizardPage) page;
      if (p.isActivePage()) {
        return super.canFinish() && p.canFinish();
      }
    }
    return super.canFinish();
  }

  @Override
  public boolean performFinish() {
    CBWebAppWizardFinishOperation operation = new CBWebAppWizardFinishOperation(this);
    return operation.performFinish();
  }

  private IWorkbenchPart getActivePart() {
    IWorkbenchWindow activeWindow = getWorkbench().getActiveWorkbenchWindow();
    if (activeWindow != null) {
      IWorkbenchPage activePage = activeWindow.getActivePage();
      if (activePage != null) {
        return activePage.getActivePart();
      }
    }
    return null;
  }

  public CBProjectNameAndLocationPage getNameAndLocationPage() {
    return this.nameAndLocPage;
  }

  public CBServicesWizardPage getServicesPage() {
    return this.servicesPage;
  }

  @Override
  protected void initializeDefaultPageImageDescriptor() {
    ImageDescriptor descriptor = CBRunUiActivator.imageDescriptorFromPlugin(CBRunUiActivator.PLUGIN_ID,
        Images.CLOUDBEES_WIZ_ICON_PATH);
    setDefaultPageImageDescriptor(descriptor);
  }

}
