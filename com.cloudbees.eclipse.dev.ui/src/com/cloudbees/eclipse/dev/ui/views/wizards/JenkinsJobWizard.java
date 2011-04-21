package com.cloudbees.eclipse.dev.ui.views.wizards;

import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CoreScripts;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.wizard.CBWizardSupport;

public class JenkinsJobWizard extends Wizard {

  private static final String WINDOW_TITLE = "Jenkins Job";

  private final IProject project;
  private JenkinsJobWizardPage jenkinsPage;

  public JenkinsJobWizard(IProject project) {
    this.project = project;
    setNeedsProgressMonitor(true);
    setWindowTitle(WINDOW_TITLE);
    setDefaultPageImageDescriptor(CloudBeesUIPlugin.imageDescriptorFromPlugin(CloudBeesUIPlugin.PLUGIN_ID,
        "icons/cb_wiz_icon.png"));
  }

  @Override
  public void addPages() {
    this.jenkinsPage = new JenkinsJobWizardPage(this.project);
    addPage(this.jenkinsPage);
  }

  @Override
  public boolean performFinish() {
    JenkinsInstance instance = this.jenkinsPage.getJenkinsInstance();
    String jobName = this.jenkinsPage.getJobName();

    try {
      CBWizardSupport.makeJenkinsJob(CoreScripts.getMockConfigXML(), instance, jobName, getContainer());
      PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(instance.url));
      return true;
    } catch (Exception e) {
      e.printStackTrace(); // TODO
      return false;
    }

  }
}
