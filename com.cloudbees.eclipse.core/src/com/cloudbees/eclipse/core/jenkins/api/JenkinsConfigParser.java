package com.cloudbees.eclipse.core.jenkins.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;

public class JenkinsConfigParser {

  public static JenkinsScmConfig parse(final String configContent) {
    try {
      return parseConfigXml(new ByteArrayInputStream(configContent.getBytes("UTF-8")));
    } catch (Exception e) {
      throw new RuntimeException(e); // TODO
    }
  }

  public static JenkinsScmConfig parse(final InputStream input) {
    try {
      return parseConfigXml(input);
    } catch (Exception e) {
      throw new RuntimeException(e); // TODO
    }
  }

  private static JenkinsScmConfig parseConfigXml(final InputStream input) {
    try {

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document dom = db.parse(input);

      Element root = dom.getDocumentElement();

      NodeList scmNodes = root.getElementsByTagName("scm");

      List<JenkinsScmConfig.Repository> repos = new ArrayList<JenkinsScmConfig.Repository>();

      for (int i = 0; scmNodes != null && i < scmNodes.getLength(); i++) {
        Node scmNode = scmNodes.item(i);
        findGit(scmNode, repos);
        findSvn(scmNode, repos);
        findCvs(scmNode, repos);
      }

      JenkinsScmConfig config = new JenkinsScmConfig();
      config.repos = repos.toArray(new JenkinsScmConfig.Repository[repos.size()]);
      return config;

    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e); // TODO
    } catch (SAXException e) {
      throw new RuntimeException(e); // TODO
    } catch (IOException e) {
      throw new RuntimeException(e); // TODO
    }
  }

  private static void findGit(final Node scmNode, final List<JenkinsScmConfig.Repository> repos) {
    List<JenkinsScmConfig.Repository> gitRepos = new ArrayList<JenkinsScmConfig.Repository>();

    NodeList remotes = ((Element) scmNode).getElementsByTagName("org.spearce.jgit.transport.RemoteConfig");
    for (int i = 0; remotes != null && i < remotes.getLength(); i++) {
      Node remote = remotes.item(i);
      JenkinsScmConfig.Repository repo = new JenkinsScmConfig.Repository();
      repo.type = ForgeInstance.TYPE.GIT;
      repos.add(repo);
      gitRepos.add(repo);

      NodeList params = ((Element) remote).getElementsByTagName("string");
      for (int k = 0; params != null && k < params.getLength(); k++) {
        Node param = params.item(k);
        if (param.getFirstChild() != null && "url".equals(param.getFirstChild().getNodeValue())
            && k + 1 < params.getLength()) {
          repo.url = params.item(k + 1).getFirstChild().getNodeValue();
        }
        // TODO origin and fetch?
      }
    }

    //    List<String> branches = new ArrayList<String>();
    //    NodeList branchNodes = ((Element) scmNode).getElementsByTagName("hudson.plugins.git.BranchSpec");
    //    for (int i = 0; branchNodes != null && i < branchNodes.getLength(); i++) {
    //      Node branchNode = branchNodes.item(i);
    //      Node firstChild = branchNode.getChildNodes().item(0).getFirstChild();
    //      String branch = firstChild.getNodeValue();
    //      branches.add(branch);
    //    }
    //
    //    if (!branches.isEmpty()) {
    //      for (JenkinsScmConfig.Repository repo : gitRepos) {
    //        repo.branches = branches.toArray(new String[branches.size()]);
    //      }
    //    }
  }

  private static void findSvn(final Node scmNode, final List<JenkinsScmConfig.Repository> repos) {
    NodeList remotes = ((Element) scmNode).getElementsByTagName("remote");
    for (int i = 0; remotes != null && i < remotes.getLength(); i++) {
      Node remote = remotes.item(i);
      JenkinsScmConfig.Repository repo = new JenkinsScmConfig.Repository();
      repo.type = ForgeInstance.TYPE.SVN;
      repo.url = remote.getFirstChild().getNodeValue();
      repos.add(repo);
    }
  }

  private static void findCvs(final Node scmNode, final List<JenkinsScmConfig.Repository> repos) {
    String scm = getTextValue(((Element) scmNode), "cvsroot");
    if (scm != null) {
      JenkinsScmConfig.Repository repo = new JenkinsScmConfig.Repository();
      repo.type = ForgeInstance.TYPE.CVS;
      repo.url = scm;
      repos.add(repo);
    }
  }

  private static String getTextValue(final Element ele, final String tagName) {
    String textVal = null;
    NodeList nl = ele.getElementsByTagName(tagName);
    if (nl != null && nl.getLength() > 0) {
      Element el = (Element) nl.item(0);
      textVal = el.getFirstChild().getNodeValue();
    }

    return textVal;
  }

  private static int getIntValue(final Element ele, final String tagName) {
    return Integer.parseInt(getTextValue(ele, tagName));
  }
}
