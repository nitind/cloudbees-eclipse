package com.cloudbees.eclipse.run.core;

import java.io.FileNotFoundException;
import java.io.OutputStream;

import org.apache.tools.ant.listener.TimestampedLogger;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.api.ApplicationStatusResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.GrandCentralService.AuthInfo;
import com.cloudbees.eclipse.run.sdk.CBSdkActivator;

public class BeesSDK {

  public static final String API_URL = "https://api.cloudbees.com/api";

  public static ApplicationListResponse getList() throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return new ApplicationListResponse();
    }
    return client.applicationList();
  }

  public static ApplicationInfo getServerState(final String accountName, final String id) throws CloudBeesException,
      Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return null;
    }
    return client.applicationInfo(accountName + "/" + id);
  }

  public static ApplicationStatusResponse stop(final String accountName, final String id) throws CloudBeesException,
      Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return null;
    }
    ApplicationStatusResponse applicationStop = client.applicationStop(accountName + "/" + id);
    update();
    return applicationStop;
  }

  private static void update() throws Exception {
    new Thread("Manual AppList Update") {
      @Override
      public void run() {
        try {
          Thread.sleep(5 * 1000);
          CBRunCoreActivator.getPoller().fetchAndUpdate();
        } catch (Exception e) {
          CBRunCoreActivator.logError(e);
        }
      }
    }.start();
  }

  public static ApplicationStatusResponse start(final String accountName, final String id) throws CloudBeesException,
      Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return null;
    }
    ApplicationStatusResponse applicationStart = client.applicationStart(accountName + "/" + id);
    update();
    return applicationStart;
  }

  public static ApplicationDeployArchiveResponse deploy(final IProject project, final String account, final String id,
      final boolean build) throws CloudBeesException, CoreException, FileNotFoundException, Exception {

    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return null;
    }

    String appId = account + "/" + id;//$NON-NLS-1$

    IPath workspacePath = project.getLocation().removeLastSegments(1);
    IPath buildPath = getWarFile(project, build, appId).getFullPath();

    String warFile = workspacePath.toOSString() + buildPath.toOSString();
    ApplicationDeployArchiveResponse applicationDeployWar = client.applicationDeployWar(appId, null, null, warFile,
        null, null);
    update();

    return applicationDeployWar;
  }

  public static ApplicationDeployArchiveResponse deploy(final String appId, final String warUrl)
      throws CloudBeesException, CoreException, FileNotFoundException, Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return null;
    }

    ApplicationDeployArchiveResponse applicationDeployWar = client.applicationDeployWar(appId, null, null, warUrl,
        null, null);
    update();

    return applicationDeployWar;
  }

  /**
   * Establishes a persistent connection to an application log so that you can see new messages as they are written to
   * the logs. This is provides a "cloud-friendly" replacement for the ubiquitous "tail" command many developers use to
   * monitor/debug application log files.
   * 
   * @param appId
   * @param logName
   *          valid options are "server", "access" or "error"
   * @param outputStream
   * @throws Exception
   */
  public static void tail(final String appId, final String logName, final OutputStream outputStream) throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient beesClient = getBeesClient(grandCentralService);
    if (beesClient != null) {
      beesClient.tailLog(appId, logName, outputStream);
    }
  }

  /**
   * Delete an application
   * 
   * @param appId
   * @throws Exception
   */
  public static void delete(final String appId) throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient beesClient = getBeesClient(grandCentralService);
    if (beesClient != null) {
      beesClient.applicationDelete(appId);
    }
  }

  private static IFile getWarFile(final IProject project, final boolean build, final String id)
      throws CloudBeesException, CoreException, FileNotFoundException {
    if (build) {
      runTargets(project, new String[] { "dist" }, id);
    }
    IFile file = getBuildFolder(project, id).getFile("webapp.war");

    if (!file.exists()) {
      runTargets(project, new String[] { "dist" }, id);
      file.refreshLocal(IFile.DEPTH_INFINITE, null);

      if (!file.exists()) {
        throw new FileNotFoundException("Could not find webapp.war file in build folder .");
      }
    }

    return file;
  }

  private static IFolder getBuildFolder(final IProject project, final String id) throws CloudBeesException,
      CoreException, FileNotFoundException {

    IFolder folder = project.getFolder("build");
    if (!folder.exists()) {
      runTargets(project, new String[] { "dist" }, id);
      folder.refreshLocal(IFile.DEPTH_INFINITE, null);

      if (!folder.exists()) {
        throw new FileNotFoundException(
            "Unexpected project structure. Could not find folder \"build\" in project root.");
      }
    }

    return folder;
  }

  private static BeesClient getBeesClient(final GrandCentralService grandCentralService) throws CloudBeesException {
    System.out.println();
    if (grandCentralService.hasAuthInfo()) {
      AuthInfo cachedAuthInfo = grandCentralService.getCachedAuthInfo(false);

      String api_key = cachedAuthInfo.getAuth().api_key;
      String secret_key = cachedAuthInfo.getAuth().secret_key;

      BeesClientConfiguration conf = new BeesClientConfiguration(API_URL, api_key, secret_key, "xml", "1.0");
      return new BeesClient(conf);
    } else {
      return null;
    }
  }

  private static void runTargets(final IProject project, final String[] targets, final String id)
      throws CloudBeesException, CoreException {
    AntRunner runner = new AntRunner();

    runner.setBuildFileLocation(getBuildXmlPath(project));
    runner.setExecutionTargets(targets);

    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    AuthInfo cachedAuthInfo = grandCentralService.getCachedAuthInfo(false);

    String secretKey = "-Dbees.apiSecret=" + cachedAuthInfo.getAuth().secret_key;//$NON-NLS-1$
    String authKey = " -Dbees.apiKey=" + cachedAuthInfo.getAuth().api_key;//$NON-NLS-1$
    String appId = " -Dbees.appid=" + id;
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
  private static String getBuildXmlPath(final IProject project) {
    IPath workspacePath = project.getLocation().removeLastSegments(1);
    IPath buildPath = project.getFile("build.xml").getFullPath();

    return workspacePath.toOSString() + buildPath.toOSString();
  }

}
