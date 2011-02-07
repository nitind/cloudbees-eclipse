package com.cloudbees.eclipse.ui.internal.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.NectarService;
import com.cloudbees.eclipse.core.domain.NectarInstance;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.internal.wizard.NectarFinishPage;
import com.cloudbees.eclipse.ui.internal.wizard.NectarWizard;

public class NectarWizardDialog extends WizardDialog {

  public NectarWizardDialog(Shell parent) {
    super(parent, new NectarWizard());
  }

  public NectarWizardDialog(Shell parent, NectarInstance ni) {
    super(parent, new NectarWizard(ni));
  }

  @Override
  protected void nextPressed() {
    if ("url".equals(getCurrentPage().getName())) {
      try {
        run(false, true, new IRunnableWithProgress() {
          
          public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.setTaskName("Validating Jenkins URL...");

            NectarService ns = CloudBeesUIPlugin.getDefault().lookupNectarService(
                ((NectarWizard) getWizard()).getNectarInstance());

            try {
              ns.getInstance(monitor);
              ((NectarFinishPage) ((NectarWizard) getWizard()).getPage("finish")).initText(null);
            } catch (CloudBeesException e) {
              ((NectarFinishPage) ((NectarWizard) getWizard()).getPage("finish")).initText(e);

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
