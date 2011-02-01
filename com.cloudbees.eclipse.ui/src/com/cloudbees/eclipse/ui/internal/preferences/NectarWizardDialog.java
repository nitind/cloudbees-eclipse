package com.cloudbees.eclipse.ui.internal.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.cloudbees.eclipse.ui.internal.wizard.NectarWizard;

public class NectarWizardDialog extends WizardDialog {

  public NectarWizardDialog(Shell parent) {
    super(parent, new NectarWizard());
  }

  @Override
  protected void nextPressed() {
    System.out.println("NEXT PRESSED "+getCurrentPage().getName());
    if ("url".equals(getCurrentPage().getName())) {
      System.out.println("Validating connection!");
      try {
        run(false, true, new IRunnableWithProgress() {
          
          public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.setTaskName("Validating Nectar URL...");
            Thread.currentThread().sleep(2000);
            monitor.done();
          }

        });
      } catch (InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
    }
    super.nextPressed();
  }

}
