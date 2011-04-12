package com.cloudbees.eclipse.dev.ui.internal.action;

import java.io.ByteArrayInputStream;
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
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
          public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            try {
              String testReport = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(build.url).getTestReport(build.url, monitor);
              if (testReport == null) {
                StatusManager.getManager().handle(
                    new Status(IStatus.ERROR, CloudBeesDevUiPlugin.PLUGIN_ID, "The build did not produce test results."),
                    StatusManager.SHOW | StatusManager.BLOCK);
              }

              if (monitor.isCanceled()) {
                throw new InterruptedException();
              }

              showInJUnitView(testReport, monitor);
            } catch (Exception e) {
              throw new InvocationTargetException(e);
            }
          }
        });
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

  private void showInJUnitView(final String testReport, final IProgressMonitor monitor) throws Exception {
    //System.out.println("TESTREPORT!\n" + testReport);
    // TODO transform Jenkins test results into JUnit standard test results
    //    String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><testsuite errors=\"0\" failures=\"1\" tests=\"5\" name=\"blah\"> </testsuite>";

    InputStream in = new ByteArrayInputStream(testReport.getBytes("UTF-8"));

    final TestRunSession testRunSession = JUnitReportSupport.importJenkinsTestRunSession(in);

    //          JUnitResultGenerator generator = new JUnitResultGenerator(build.getTestResult());
    //          TestRunHandler handler = new TestRunHandler(testRunSession);
    //          try {
    //            generator.write(handler);
    //          } catch (SAXException e) {
    //            throw new CoreException(new Status(IStatus.ERROR, CloudBeesDevUiPlugin.PLUGIN_ID,
    //                "Unexpected parsing error while preparing test results", e));
    //          }

    CloudBeesDevUiPlugin.getDefault().showView(TestRunnerViewPart.NAME);
    JUnitReportSupport.getJUnitModel().addTestRunSession(testRunSession);
  }


}
