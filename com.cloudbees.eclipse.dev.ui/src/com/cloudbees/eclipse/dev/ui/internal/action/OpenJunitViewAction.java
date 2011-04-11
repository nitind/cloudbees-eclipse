package com.cloudbees.eclipse.dev.ui.internal.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.junit.JUnitCorePlugin;
import org.eclipse.jdt.internal.junit.Messages;
import org.eclipse.jdt.internal.junit.model.JUnitModel;
import org.eclipse.jdt.internal.junit.model.ModelMessages;
import org.eclipse.jdt.internal.junit.model.TestRunHandler;
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.statushandlers.StatusManager;
import org.xml.sax.SAXException;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class OpenJunitViewAction extends BaseSelectionListenerAction {

  public OpenJunitViewAction() {
    super("Show Test Results");
    setToolTipText("Show Test Results in JUnit View");
    setImageDescriptor(CloudBeesDevUiPlugin.getImageDescription(CBImages.IMG_JUNIT));
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

    System.out.println("TESTREPORT!\n" + testReport);
    try {
      // TODO transform Jenkins test results into JUnit standard test results
      String result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><testsuite errors=\"0\" failures=\"1\" tests=\"5\" name=\"blah\"> </testsuite>";

      InputStream in = new ByteArrayInputStream(result.getBytes("UTF-8"));

      final TestRunSession testRunSession = importTestRunSession(in);

      //          JUnitResultGenerator generator = new JUnitResultGenerator(build.getTestResult());
      //          TestRunHandler handler = new TestRunHandler(testRunSession);
      //          try {
      //            generator.write(handler);
      //          } catch (SAXException e) {
      //            throw new CoreException(new Status(IStatus.ERROR, CloudBeesDevUiPlugin.PLUGIN_ID,
      //                "Unexpected parsing error while preparing test results", e));
      //          }

      CloudBeesDevUiPlugin.getDefault().showView(TestRunnerViewPart.NAME);
      getJUnitModel().addTestRunSession(testRunSession);
    } catch (CoreException e) {
      StatusManager.getManager()
      .handle(
          new Status(IStatus.ERROR, CloudBeesDevUiPlugin.PLUGIN_ID,
              "Unexpected error while processing test results", e), StatusManager.SHOW | StatusManager.LOG);
      return;
    }

  }

  private static volatile JUnitModel junitModel;

  static JUnitModel getJUnitModel() {
    if (junitModel == null) {
      try {
        // Eclipse 3.6 or later
        Class<?> clazz;
        try {
          clazz = Class.forName("org.eclipse.jdt.internal.junit.JUnitCorePlugin");
        } catch (ClassNotFoundException e) {
          // Eclipse 3.5 and earlier
          clazz = Class.forName("org.eclipse.jdt.internal.junit.ui.JUnitPlugin");
        }

        Method method = clazz.getDeclaredMethod("getModel");
        junitModel = (JUnitModel) method.invoke(null);
      } catch (Exception e) {
        NoClassDefFoundError error = new NoClassDefFoundError("Unable to locate container for JUnitModel");
        error.initCause(e);
        throw error;
      }
    }
    return junitModel;
  }

  public static TestRunSession importTestRunSession(final InputStream in) throws CoreException {
    try {
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      //      parserFactory.setValidating(true); // TODO: add DTD and debug flag
      SAXParser parser = parserFactory.newSAXParser();
      TestRunHandler handler = new TestRunHandler();
      parser.parse(in, handler);
      TestRunSession session = handler.getTestRunSession();
      //      JUnitCorePlugin.getModel().addTestRunSession(session);
      return session;
    } catch (ParserConfigurationException e) {
      throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JUnitCorePlugin.getPluginId(),
          Messages.format(ModelMessages.JUnitModel_could_not_read, "bla"), e)); // TODO
    } catch (SAXException e) {
      throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JUnitCorePlugin.getPluginId(),
          Messages.format(ModelMessages.JUnitModel_could_not_read, "bla"), e)); // TODO
    } catch (IOException e) {
      throw new CoreException(new org.eclipse.core.runtime.Status(IStatus.ERROR, JUnitCorePlugin.getPluginId(),
          Messages.format(ModelMessages.JUnitModel_could_not_read, "bla"), e)); // TODO
    }
  }

}
