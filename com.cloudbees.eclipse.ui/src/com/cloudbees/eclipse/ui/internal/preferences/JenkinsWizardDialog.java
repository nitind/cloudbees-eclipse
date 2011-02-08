package com.cloudbees.eclipse.ui.internal.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.domain.JenkinsInstance;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.internal.wizard.JenkinsFinishPage;
import com.cloudbees.eclipse.ui.internal.wizard.JenkinsWizard;

public class JenkinsWizardDialog extends WizardDialog {

  public JenkinsWizardDialog(Shell parent) {
    super(parent, new JenkinsWizard());
  }

  public JenkinsWizardDialog(Shell parent, JenkinsInstance ni) {
    super(parent, new JenkinsWizard(ni));
  }

  @Override
  protected void nextPressed() {
    if ("url".equals(getCurrentPage().getName())) {
      try {
        run(false, true, new IRunnableWithProgress() {
          
          public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.setTaskName("Validating Jenkins URL...");

            JenkinsService ns = CloudBeesUIPlugin.getDefault().lookupJenkinsService(
                ((JenkinsWizard) getWizard()).getJenkinsInstance());

            try {
              ns.getInstance(monitor);
              ((JenkinsFinishPage) ((JenkinsWizard) getWizard()).getPage("finish")).initText(null);
            } catch (CloudBeesException e) {
              ((JenkinsFinishPage) ((JenkinsWizard) getWizard()).getPage("finish")).initText(e);

              e.printStackTrace(); // TODO log
            }

            monitor.done();
          }

        });
      } catch (InvocationTargetException e) {
        e.printStackTrace(); // TODO log
      } catch (InterruptedException e) {
        e.printStackTrace(); // TODO log
      }
      
    }
    super.nextPressed();
  }

}
