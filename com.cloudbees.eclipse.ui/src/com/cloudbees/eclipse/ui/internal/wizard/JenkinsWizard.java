package com.cloudbees.eclipse.ui.internal.wizard;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class JenkinsWizard extends Wizard {

  private final JenkinsInstance instance;

  private final JenkinsUrlPage pageUrl;
  private final JenkinsFinishPage pageFinish;

  public JenkinsWizard() {
    this(new JenkinsInstance());
  }

  public JenkinsWizard(final JenkinsInstance ni) {
    this.instance = ni;

    setNeedsProgressMonitor(true);
    ImageDescriptor id = ImageDescriptor.createFromURL(CloudBeesUIPlugin.getDefault().getBundle()
        .getResource("/icons/cb_wiz_icon2.png"));
    setDefaultPageImageDescriptor(id);
    if (ni.label == null) {
      setWindowTitle("New Jenkins instance");
    } else {
      setWindowTitle("Edit Jenkins instance");
    }
    setForcePreviousAndNextButtons(true);
    setHelpAvailable(false);

    this.pageUrl = new JenkinsUrlPage(ni);
    this.pageFinish = new JenkinsFinishPage(ni);
  }

  @Override
  public void addPages() {
    addPage(this.pageUrl);
    addPage(this.pageFinish);
  }

  @Override
  public boolean performFinish() {
    saveInstanceInfo();
    return true;
  }

  public JenkinsInstance getJenkinsInstance() {
    return this.instance;
  }

  private void saveInstanceInfo() {

    // TODO shouldn't we actually save only when Apply clicked on the preference page?
    CloudBeesUIPlugin.getDefault().saveJenkinsInstance(this.instance);

  }

}
