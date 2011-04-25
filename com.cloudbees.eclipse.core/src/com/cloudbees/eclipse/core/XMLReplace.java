package com.cloudbees.eclipse.core;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XMLReplace {

  private final File xml;
  private final Map<String, String> replacements;

  private XMLReplace(File xml) {
    this.xml = xml;
    this.replacements = new HashMap<String, String>();
  }

  public static XMLReplace getInstance(File xml) {
    return new XMLReplace(xml);
  }

  public static XMLReplace getInstance(String pathToXML) {
    File xml = new File(pathToXML);
    if (!xml.exists() || !xml.isFile() || !pathToXML.endsWith("xml")) {
      throw new IllegalArgumentException("Invalid path: " + pathToXML + ". Does not exists or not a xml file.");
    }
    return new XMLReplace(xml);
  }

  public XMLReplace addReplacement(String node, String value) {
    this.replacements.put(node, value);
    return this;
  }

  public String replaceToString() throws Exception {
    StreamResult result = new StreamResult(new StringWriter());
    return ((StringWriter) replace(result).getWriter()).getBuffer().toString();
  }

  public StreamResult replace(StreamResult resultHolder) throws Exception {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this.xml);
    XPath xpath = XPathFactory.newInstance().newXPath();

    Iterator<Entry<String, String>> iterator = this.replacements.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, String> entry = iterator.next();
      NodeList nodes = (NodeList) xpath.evaluate("//" + entry.getKey(), document, XPathConstants.NODESET);
      for (int i = 0; i < nodes.getLength(); i++) {
        nodes.item(i).setTextContent(entry.getValue());
      }
    }

    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(new DOMSource(document), resultHolder);
    return resultHolder;
  }
}
