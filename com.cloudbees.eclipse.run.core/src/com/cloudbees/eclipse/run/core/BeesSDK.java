package com.cloudbees.eclipse.run.core;

import java.io.FileNotFoundException;

import org.apache.tools.ant.listener.TimestampedLogger;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.api.ApplicationStatusResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.GrandCentralService.AuthInfo;
import com.cloudbees.eclipse.sdk.CBSdkActivator;

public class BeesSDK {

  public static final String API_URL = "https://api.cloudbees.com/api";

  public ApplicationStatusResponse stop(IProject project) throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);

    String appId = grandCentralService.getCachedPrimaryUser(false) + "/" + project.getName();//$NON-NLS-1$

    return client.applicationStop(appId);
  }

  public ApplicationStatusResponse start(IProject project) throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);

    String appId = grandCentralService.getCachedPrimaryUser(false) + "/" + project.getName();//$NON-NLS-1$

    return client.applicationStart(appId);
  }

  /**
   * Deploys project to the cloud if it has standard Bees project layout. NB! If the war file or build directory isn't
   * found, it runs "ant dist".
   * 
   * @param project
   * @return
   * @throws Exception
   */
  public ApplicationDeployArchiveResponse deploy(IProject project) throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);

    String appId = grandCentralService.getCachedPrimaryUser(false) + "/" + project.getName();//$NON-NLS-1$

    IPath workspacePath = project.getLocation().removeLastSegments(1);
    IPath buildPath = getWarFile(project).getFullPath();

    String warFile = workspacePath.toOSString() + buildPath.toOSString();
    return client.applicationDeployWar(appId, null, null, warFile, null, null);
  }

  private IFile getWarFile(IProject project) throws CloudBeesException, CoreException, FileNotFoundException {
    IFile file = getBuildFolder(project).getFile("webapp.war");

    if (!file.exists()) {
      runTargets(project, new String[] { "dist" });
      file.refreshLocal(IFile.DEPTH_INFINITE, null);

      if (!file.exists()) {
        throw new FileNotFoundException("Could not find webapp.war file in build folder .");
      }
    }

    return file;
  }

  private IFolder getBuildFolder(IProject project) throws CloudBeesException, CoreException, FileNotFoundException {

    IFolder folder = project.getFolder("build");
    if (!folder.exists()) {
      runTargets(project, new String[] { "dist" });
      folder.refreshLocal(IFolder.DEPTH_INFINITE, null);

      if (!folder.exists()) {
        throw new FileNotFoundException(
            "Unexpected project structure. Could not find folder \"build\" in project root.");
      }
    }

    return folder;
  }

  private BeesClient getBeesClient(GrandCentralService grandCentralService) throws CloudBeesException {
    AuthInfo cachedAuthInfo = grandCentralService.getCachedAuthInfo(false);

    String api_key = cachedAuthInfo.getAuth().api_key;
    String secret_key = cachedAuthInfo.getAuth().secret_key;

    BeesClientConfiguration conf = new BeesClientConfiguration(API_URL, api_key, secret_key, "xml", "1.0");
    BeesClient client = new BeesClient(conf);
    return client;
  }

  private void runTargets(IProject project, String[] targets) throws CloudBeesException, CoreException {
    AntRunner runner = new AntRunner();

    runner.setBuildFileLocation(getBuildXmlPath(project));
    runner.setExecutionTargets(targets);

    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    AuthInfo cachedAuthInfo = grandCentralService.getCachedAuthInfo(false);

    String secretKey = "-Dbees.apiSecret=" + cachedAuthInfo.getAuth().secret_key;//$NON-NLS-1$
    String authKey = " -Dbees.apiKey=" + cachedAuthInfo.getAuth().api_key;//$NON-NLS-1$
    String appId = " -Dbees.appid=" + grandCentralService.getCachedPrimaryUser(false) + "/" + project.getName();//$NON-NLS-1$
    String beesHome = " -Dbees.home=" + CBSdkActivator.getDefault().getBeesHome();
    runner.setArguments(secretKey + authKey + appId + beesHome);

    runner.addBuildLogger(TimestampedLogger.class.getName());
    runner.run();
  }

  /**
   * Construct full path for the build.xml
   * 
   * @param project
   * @return
   */
  private String getBuildXmlPath(IProject project) {
    IPath workspacePath = project.getLocation().removeLastSegments(1);
    IPath buildPath = project.getFile("build.xml").getFullPath();

    return workspacePath.toOSString() + buildPath.toOSString();
  }

}
