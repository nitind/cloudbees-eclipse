package com.cloudbees.eclipse.run.ui.popup.actions;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.core.launchconfiguration.CBLaunchConfigurationConstants;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

@SuppressWarnings("restriction")
public class DeployAction implements IObjectActionDelegate {

  private final class IRunnableWithProgressImplementation implements IRunnableWithProgress {
    private final Object firstElement;

    private IRunnableWithProgressImplementation(Object firstElement) {
      this.firstElement = firstElement;
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      monitor.beginTask("Deploying to RUN@cloud", 1);
      try {
        ApplicationDeployArchiveResponse deploy = BeesSDK.deploy((IProject) this.firstElement, true);
        monitor.done();
        boolean openConfirm = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Deploy to RUN@cloud",
            "Deployment finished to RUN@cloud. Open " + deploy.getUrl());

        if (openConfirm) {
          openBrowser(deploy);
        }
      } catch (Exception e) {
        Status status = new Status(IStatus.ERROR, CBRunUiActivator.PLUGIN_ID, e.getMessage());
        CBRunUiActivator.getDefault().getLog().log(status);
      }

    }
  }

  /**
   * Constructor for Action1.
   */
  public DeployAction() {
    super();
  }

  /**
   * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
   */
  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

  /**
   * @see IActionDelegate#run(IAction)
   */
  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {

      ISelection selection = ((ObjectPluginAction) action).getSelection();

      if (selection instanceof StructuredSelection) {
        final Object firstElement = ((StructuredSelection) selection).getFirstElement();

        if (firstElement instanceof IProject) {
          try {
            ProgressMonitorDialog monitor = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
            monitor.run(false, false, new IRunnableWithProgressImplementation(firstElement));
            CloudBeesUIPlugin.getDefault().fireApplicationInfoChanged();
          } catch (Exception e) {
            CBRunUiActivator.logError(e);
          }
        }
      }
    }
  }

  private void openBrowser(ApplicationDeployArchiveResponse deploy) {
    final String url = deploy.getUrl();
    if (url != null) {
      Display.getCurrent().asyncExec(new Runnable() {

        @Override
        public void run() {
          IWebBrowser browser;
          try {
            browser = PlatformUI.getWorkbench().getBrowserSupport()
                .createBrowser(CBLaunchConfigurationConstants.COM_CLOUDBEES_ECLIPSE_WST);
            Thread.sleep(2000);
            browser.openURL(new URL(url));
          } catch (Exception e) {
            CBRunUiActivator.logError(e);
          }

        }
      });
    }
  }

  /**
   * @see IActionDelegate#selectionChanged(IAction, ISelection)
   */
  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

}
