package com.cloudbees.eclipse.ui.internal.wizard;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

import com.cloudbees.eclipse.core.domain.NectarInstance;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class NectarWizard extends Wizard {

  private final NectarInstance ni;

  private final NectarUrlPage pageUrl = new NectarUrlPage();
  private final NectarFinishPage pageFinish = new NectarFinishPage();

  public NectarWizard() {
    this(new NectarInstance());
  }

  public NectarWizard(final NectarInstance ni) {
    this.ni = ni;

    setNeedsProgressMonitor(true);
    ImageDescriptor id = ImageDescriptor.createFromURL(CloudBeesUIPlugin.getDefault().getBundle()
        .getResource("/icons/cb_wiz_icon2.png"));
    setDefaultPageImageDescriptor(id);
    if (ni.label == null) {
      setWindowTitle("New Nectar instance");
    } else {
      setWindowTitle("Edit Nectar instance");
    }
    setForcePreviousAndNextButtons(true);
    setHelpAvailable(false);

    pageUrl.setNectarInstance(ni);
    pageFinish.setNectarInstance(ni);
  }

  @Override
  public void addPages() {
    addPage(pageUrl);
    addPage(pageFinish);
  }

  @Override
  public boolean performFinish() {
    saveInstanceInfo();
    return true;
  }

  public NectarInstance getNectarInstance() {
    return ni;
  }

  private void saveInstanceInfo() {

    // FIXME shouldn't we actually save only when Apply clicked on the preference page? 
    CloudBeesUIPlugin.getDefault().saveNectarInstance(ni);

  }

}
