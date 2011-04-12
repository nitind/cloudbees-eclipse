package com.cloudbees.eclipse.dev.core.junit;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXResult;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.junit.JUnitCorePlugin;
import org.eclipse.jdt.internal.junit.Messages;
import org.eclipse.jdt.internal.junit.model.IXMLTags;
import org.eclipse.jdt.internal.junit.model.JUnitModel;
import org.eclipse.jdt.internal.junit.model.ModelMessages;
import org.eclipse.jdt.internal.junit.model.TestRunHandler;
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.osgi.framework.Bundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.cloudbees.eclipse.dev.core.CloudBeesDevCorePlugin;

public class JUnitReportSupport {

  private static volatile JUnitModel junitModel;

  public static TestRunSession importJenkinsTestRunSession(final String jobName, final String projectName, final InputStream testReport) throws Exception {
    InputStream xsl;
    if (CloudBeesDevCorePlugin.getDefault() != null) {
      String path = "/" + JUnitReportSupport.class.getPackage().getName().replace('.', '/') + "/jenkins-to-junit.xsl";
      Bundle bundle = Platform.getBundle(CloudBeesDevCorePlugin.PLUGIN_ID);
      xsl = bundle.getResource(path).openStream();
    } else {
      xsl = JUnitReportSupport.class.getResourceAsStream("jenkins-to-junit.xsl");
    }
    return importJenkinsTestRunSession(jobName, projectName, testReport, xsl);
  }

  public static TestRunSession importJenkinsTestRunSession(final String jobName, final String projectName, final InputStream testReport, final InputStream transform)
  throws Exception {
    TestRunHandler handler = new TestRunHandler() {
      @Override
      public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
      throws SAXException {
        if (IXMLTags.NODE_TESTRUN.equals(qName)
            || (IXMLTags.NODE_TESTSUITE.equals(qName) && "TestRunAllTests".equals(attributes
                .getValue(IXMLTags.ATTR_NAME)))) {
          Attributes newAttrs = new Attributes() {
            public int getLength() {
              return attributes.getLength();
            }

            public String getURI(final int index) {
              return attributes.getURI(index);
            }

            public String getLocalName(final int index) {
              return attributes.getLocalName(index);
            }

            public String getQName(final int index) {
              return attributes.getQName(index);
            }

            public String getType(final int index) {
              return attributes.getType(index);
            }

            public String getValue(final int index) {
              return attributes.getValue(index);
            }

            public int getIndex(final String uri, final String localName) {
              return attributes.getIndex(uri, localName);
            }

            public int getIndex(final String qName) {
              return attributes.getIndex(qName);
            }

            public String getType(final String uri, final String localName) {
              return attributes.getType(uri, localName);
            }

            public String getType(final String qName) {
              return attributes.getType(qName);
            }

            public String getValue(final String uri, final String localName) {
              return attributes.getValue(uri, localName);
            }

            public String getValue(final String qName) {
              if (IXMLTags.ATTR_NAME.equals(qName) && jobName != null) {
                return jobName;
              }
              if (IXMLTags.ATTR_PROJECT.equals(qName) && projectName != null) {
                return projectName;
              }

              return attributes.getValue(qName);
            }
          };

          super.startElement(uri, localName, qName, newAttrs);
        } else {
          super.startElement(uri, localName, qName, attributes);
        }
      }
    };

    javax.xml.transform.Source xmlSource = new javax.xml.transform.stream.StreamSource(testReport);
    javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(transform);
    javax.xml.transform.Result result = //new javax.xml.transform.stream.StreamResult(System.out);
      new SAXResult(handler);

    javax.xml.transform.TransformerFactory transFact = javax.xml.transform.TransformerFactory.newInstance();
    javax.xml.transform.Transformer trans = transFact.newTransformer(xsltSource);

    trans.transform(xmlSource, result);

    TestRunSession session = handler.getTestRunSession();

    return session;

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

  public static JUnitModel getJUnitModel() {
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

  public static TestRunSession importJUnitTestRunSession(final InputStream in) throws CoreException {
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
