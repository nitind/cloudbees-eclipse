package com.cloudbees.eclipse.dev.ui.internal.action;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.statushandlers.StatusManager;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.dev.core.junit.JUnitReportSupport;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class OpenJunitViewAction extends BaseSelectionListenerAction {

  public OpenJunitViewAction() {
    super("Show Test Results");
    setToolTipText("Show Test Results in JUnit View");
    if (CloudBeesDevUiPlugin.getDefault() != null && CloudBeesDevUiPlugin.getDefault().getImageRegistry() != null
        && CBImages.IMG_JUNIT != null) {
      setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_JUNIT));
    }
  }

  public static boolean isJUnitAvailable() {
    return Platform.getBundle("org.eclipse.jdt.junit.core") != null; //$NON-NLS-1$
  }

  @Override
  protected boolean updateSelection(final IStructuredSelection selection) {
    return isJUnitAvailable();
  }

  @Override
  public void run() {

    IStructuredSelection sel = getStructuredSelection();
    Object selection = sel.getFirstElement();

    System.out.println("Show test results: " + selection);

    if (selection instanceof JenkinsBuildDetailsResponse) {
      final JenkinsBuildDetailsResponse build = (JenkinsBuildDetailsResponse) selection;

      try {
        IRunnableWithProgress op = new IRunnableWithProgress() {
          public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            try {
              InputStream testReport = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(build.url)
              .getTestReport(build.url, monitor);
              if (testReport == null) {
                StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, CloudBeesDevUiPlugin.PLUGIN_ID, "The build did not produce test results."),
                    StatusManager.SHOW | StatusManager.BLOCK);
              }

              if (monitor.isCanceled()) {
                throw new InterruptedException();
              }

              String projectName = null; // TODO

              final TestRunSession testRunSession = JUnitReportSupport.importJenkinsTestRunSession(
                  build.fullDisplayName, projectName, testReport);
              CloudBeesDevUiPlugin.getDefault().showView(TestRunnerViewPart.NAME);
              JUnitReportSupport.getJUnitModel().addTestRunSession(testRunSession);
            } catch (Exception e) {
              throw new InvocationTargetException(e);
            }
          }
        };

        //PlatformUI.getWorkbench().getProgressService().busyCursorWhile(op);
        IProgressService service = PlatformUI.getWorkbench().getProgressService();
        service.run(false, true, op);
      } catch (InterruptedException e) {
        // cancelled
      } catch (InvocationTargetException e) {
        IStatus status;
        if (e.getCause() instanceof CoreException) {
          status = ((CoreException) e.getCause()).getStatus();
        } else {
          status = new Status(IStatus.ERROR, CloudBeesDevUiPlugin.PLUGIN_ID,
              "Unexpected error while processing test results", e);
        }
        StatusManager.getManager().handle(status, StatusManager.SHOW | StatusManager.LOG);
      }
    }
  }

}
