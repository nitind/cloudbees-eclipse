package com.cloudbees.eclipse.dev.ui.internal.action;

import java.io.BufferedInputStream;
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
import com.cloudbees.eclipse.core.util.Utils;
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
  }

  public static void main(final String[] args) {
    try {
      String report = Utils.readString(new BufferedInputStream(OpenJunitViewAction.class
          .getResourceAsStream("/resources/testReport4.xml")));
      tr(report);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void tr(final String testReport) throws Exception {
    TestRunHandler handler = new TestRunHandler();

    javax.xml.transform.Source xmlSource = new javax.xml.transform.stream.StreamSource(new ByteArrayInputStream(
        testReport.getBytes("UTF-8")));
    javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(
        OpenJunitViewAction.class.getResourceAsStream(
"/resources/jenkins-to-junit.xsl"));
    javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(System.out);
    //new SAXResult(handler);

    javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance();
    javax.xml.transform.Transformer trans = transFact.newTransformer(xsltSource);

    trans.transform(xmlSource, result);

    TestRunSession session = handler.getTestRunSession();

    // // Instantiate a TransformerFactory.
    //    javax.xml.transform.TransformerFactory tFactory =
    //                        javax.xml.transform.TransformerFactory.newInstance();
    //    // Verify that the TransformerFactory implementation you are using
    //    // supports SAX input and output (Xalan-Java does!).
    //    if (tFactory.getFeature(javax.xml.transform.sax.SAXSource.FEATURE) &&
    //        tFactory.getFeature(javax.xml.transform.sax.SAXResult.FEATURE))
    //      {
    //        // Cast the TransformerFactory to SAXTransformerFactory.
    //        javax.xml.transform.sax.SAXTransformerFactory saxTFactory =
    //                       ((javax.xml.transform.sax.SAXTransformerFactory) tFactory);
    //        // Create a Templates ContentHandler to handle parsing of the
    //        // stylesheet.
    //        javax.xml.transform.sax.TemplatesHandler templatesHandler =
    //                                            saxTFactory.newTemplatesHandler();
    //
    //        org.xml.sax.XMLReader reader =
    //                       org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
    //        reader.setContentHandler(templatesHandler);
    //
    //        InputSource xslSrc = new InputSource(getClass().getResourceAsStream("resources/jenkins-to-junit.xsl"));
    //
    //        reader.parse(xslSrc);
    //
    //        // Get the Templates object (generated during the parsing of the stylesheet)
    //        // from the TemplatesHandler.
    //        javax.xml.transform.Templates templates =
    //                                              templatesHandler.getTemplates();
    //        // Create a Transformer ContentHandler to handle parsing of
    //        // the XML Source.
    //        javax.xml.transform.sax.TransformerHandler transformerHandler
    //                               = saxTFactory.newTransformerHandler(templates);
    //        // Reset the XMLReader's ContentHandler to the TransformerHandler.
    //        reader.setContentHandler(transformerHandler);
    //
    //        // Set the ContentHandler to also function as a LexicalHandler, which
    //        // can process "lexical" events (such as comments and CDATA).
    //        reader.setProperty("http://xml.org/sax/properties/lexical-handler",
    //                            transformerHandler);
    //
    ////        // Set up a Serializer to serialize the Result to a file.
    ////        org.apache.xml.serializer.Serializer serializer =
    ////        org.apache.xml.serializer.SerializerFactory.getSerializer
    ////        (org.apache.xml.serializer.OutputPropertiesFactory.getDefaultMethodProperties
    ////                                                                       ("xml"));
    ////        serializer.setOutputStream(new java.io.FileOutputStream("foo.out"));
    //
    //        // The Serializer functions as a SAX ContentHandler.
    //        javax.xml.transform.Result result =
    //          new javax.xml.transform.sax.SAXResult(serializer.asContentHandler());
    //        transformerHandler.setResult(result);
    //
    //        // Parse the XML input document.
    //        reader.parse("foo.xml");
    //      }
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
