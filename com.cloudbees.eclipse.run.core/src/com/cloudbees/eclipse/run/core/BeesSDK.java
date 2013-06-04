/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.run.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.listener.TimestampedLogger;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;

import com.cloudbees.api.ApplicationDeployArchiveResponse;
import com.cloudbees.api.ApplicationDeployArgs;
import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.api.ApplicationStatusResponse;
import com.cloudbees.api.BeesClient;
import com.cloudbees.api.BeesClientConfiguration;
import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.api.DatabaseListResponse;
import com.cloudbees.api.UploadProgress;
import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService;
import com.cloudbees.eclipse.core.GrandCentralService.AuthInfo;
import com.cloudbees.eclipse.run.sdk.CBSdkActivator;

public class BeesSDK {

  public final static String[] SUPPORTED_EXTENSIONS = { ".war", ".ear", ".zip" };

  private static final class UploadProgressWithMonitor implements UploadProgress {
    private static final String JOB_NAME = "Sent to RUN@cloud";
    private SubMonitor subMonitor = null;
    private final IProgressMonitor monitor;

    public UploadProgressWithMonitor(final IProgressMonitor monitor) {
      this.monitor = monitor;
    }

    @Override
    public void handleBytesWritten(long deltaCount, long totalWritten, long totalToSend) {
      if (this.subMonitor == null) {
        this.subMonitor = SubMonitor.convert(this.monitor, (int) totalToSend);
      }
      this.subMonitor.worked((int) deltaCount);
      this.monitor.setTaskName(JOB_NAME + " " + Math.min(totalWritten, totalToSend) + " bytes out of " + totalToSend
          + " bytes");
    }
  }

  public static final String API_URL = "https://api." + GrandCentralService.HOST + "/api";

  static {
    CBSdkActivator.getDefault().getBeesHome(); // force loading.
  }

  public static DatabaseListResponse getDatabaseList(String account) throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return new DatabaseListResponse();
    }

    return client.databaseList(account);
  }

  public static DatabaseInfo getDatabaseInfo(String dbId, boolean fetchPassword) throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      throw new CloudBeesException("Failed to locate BeesClient API");
    }
    return client.databaseInfo(dbId, fetchPassword);
  }

  public static ApplicationListResponse getList() throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return new ApplicationListResponse();
    }
    ApplicationListResponse list = client.applicationList();
/*    Iterator<ApplicationInfo> iter = list.getApplications().iterator();
    while (iter.hasNext()) {
      ApplicationInfo info = (ApplicationInfo) iter.next();
      System.out.println("SETTINGS: "+info.getSettings());
    }
*/    return list;
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
          CBRunCoreActivator.getPoller().fetchAndUpdateApps();
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
      final boolean build, IProgressMonitor monitor) throws CloudBeesException, CoreException, FileNotFoundException,
      Exception {

    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return null;
    }

    String jver = getJavaVersion(project);

    IPath workspacePath = project.getLocation().removeLastSegments(1);
    IPath buildPath = getWarFile(project, build, monitor).getFullPath();
    String warFile = workspacePath.toOSString() + buildPath.toOSString();
    String appId = getAccountAppId(account, id, client, /*warFile, */project);

    String deployType = getExtension(warFile);

    Map<String, String> params = new HashMap<String, String>();
    params.put("runtime.java_version", jver);
    ApplicationDeployArgs.Builder argBuilder = new ApplicationDeployArgs.Builder(appId)
        .deployPackage(new File(warFile), deployType).withParams(params)
        .withProgressFeedback(new UploadProgressWithMonitor(monitor));

    ApplicationDeployArchiveResponse res = client.applicationDeployArchive(argBuilder.build());

    /*    ApplicationDeployArchiveResponse applicationDeployWar = client.applicationDeployWar(appId, javaVer, null, new File(
            warFile), null, new UploadProgressWithMonitor(monitor));
    */update();

    return res;
  }

  public static Process deployProjectLocal(final IProject project, final boolean build, final boolean debug,
      String port, String debugPort, IProgressMonitor monitor) throws CloudBeesException {

    /*    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
        BeesClient client = getBeesClient(grandCentralService);
        if (client == null) {
          return;
        }
    */
    try {
      String jver = getJavaVersion(project);

      IPath workspacePath = project.getLocation().removeLastSegments(1);
      IPath buildPath = getWarFile(project, build, monitor).getFullPath();
      String warFile = workspacePath.toOSString() + buildPath.toOSString();
      //String appId = getAccountAppId(account, id, client, /*warFile, */project);

      String deployType = getExtension(warFile);

      Map<String, String> params = new HashMap<String, String>();
      params.put("runtime.java_version", jver);

      return internalDeployLocal(project, warFile, debug, port, debugPort, monitor);
    } catch (Exception e) {
      throw new CloudBeesException("Failed to deploy to local using CloudBees SDK!", e);
    }
    /*    ApplicationDeployArgs.Builder argBuilder = new ApplicationDeployArgs.Builder("")
            .deployPackage(new File(warFile), deployType).withParams(params)
            .withProgressFeedback(new UploadProgressWithMonitor(monitor));
    */

    /*    ApplicationDeployArchiveResponse applicationDeployWar = client.applicationDeployWar(appId, javaVer, null, new File(
            warFile), null, new UploadProgressWithMonitor(monitor));
    */

  }

  private static Process internalDeployLocal(IProject project, String warFile, boolean debug, String port,
      String debugPort, IProgressMonitor monitor) throws CloudBeesException, IOException {

    String cmd = "app:run";
    //System.out.println("RUNNING: " + cmd);

    //System.err.println("LAUNCHING INTERNAL " + warFile + " on " + project);

    String[] vmargs = new String[] {};

    if (debug) {
      vmargs = new String[] { "-Xdebug", "-Xnoagent", "-Djava.compiler=NONE",
          "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + debugPort };
    }

    ProcessBuilder pb = createBeesProcess(false, vmargs, cmd, warFile, "--port", port);

    //OutputStreamWriter osw = new OutputStreamWriter(out);
    //BufferedWriter writer = new BufferedWriter(osw);

    Process p = null;
    try {
      p = pb.start();
    } catch (Exception e) {
      //writer.write("Error while running CloudBees SDK: " + e.getMessage() + "\n");
      //e.printStackTrace(new PrintWriter(writer));
      throw new CloudBeesException("Failed to run CloudBees SDK!", e);
    }

    /*    InputStream stdin = p.getInputStream();

        byte[] b = new byte[4096 * 10];

        for (int n; (n = stdin.read(b)) != -1;) {
          writer.write(new String(b, 0, n, "UTF-8"));
          writer.flush();
        }

        writer.flush();
    */
    return p;
  }

  public static Process deployFileLocal(IProject project, final IFile warFile, boolean debug, String port,
      String debugPort, IProgressMonitor monitor) throws CloudBeesException {

    /*    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
        BeesClient client = getBeesClient(grandCentralService);
        if (client == null) {
          return null;
        }*/

    try {
      String jver = getJavaVersion(project);
      Map<String, String> params = new HashMap<String, String>();
      params.put("runtime.java_version", jver);

      String deployType = getExtension(warFile.getName());

      // bees app:run [options] WAR_Filename | WAR_directory
      // -e,--environment <environment>   environment configurations to load (default: run)
      //-p,--port <port>         server listen port (default: 8080)
      return internalDeployLocal(project, warFile.getRawLocation().toOSString(), debug, port, debugPort, monitor);
    } catch (Exception e) {
      throw new CloudBeesException("Failed to deploy to local using CloudBees SDK!", e);
    }
    /*
        ApplicationDeployArgs.Builder argBuilder = new ApplicationDeployArgs.Builder(accountAppId)
            .deployPackage(warFile, deployType).withParams(params)
            .withProgressFeedback(new UploadProgressWithMonitor(monitor));

        ApplicationDeployArchiveResponse res = client.applicationDeployArchive(argBuilder.build());
    */
  }

  public static String getExtension(String s) {
    if (s == null) {
      return null;
    }
    int idx = s.lastIndexOf(".");
    if (idx == -1) {
      return "";
    }
    return s.substring(idx + 1);
  }

  private static String getJavaVersion(IProject project) throws CoreException {
    IVMInstall vm = null;

    if (project == null) {
      vm = JavaRuntime.getDefaultVMInstall();
    } else if (project.hasNature(JavaCore.NATURE_ID)) {
      IJavaProject javaProject = JavaCore.create(project);
      vm = JavaRuntime.getVMInstall(javaProject);
      if (vm == null) {
        vm = JavaRuntime.getDefaultVMInstall();
      }
    }

    if (vm instanceof IVMInstall2) {
      IVMInstall2 vm2 = (IVMInstall2) vm;
      String ver = vm2.getJavaVersion();
      String[] vera = ver.split("\\.");
      if (vera.length >= 2) {
        String v = vera[0] + "." + vera[1];
        return v;
      }

    }

    return null;
  }

  public static String getAccountAppId(final String account, final String id, /*final String warPath, */
      IProject project) throws CloudBeesException, Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return null;
    }

    String appId = getAccountAppId(account, id, client, /*warPath, */project);
    return appId;
  }

  public static String getBareAppId(IProject project) throws CloudBeesException, Exception {
    /*    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
        BeesClient client = getBeesClient(grandCentralService);
        if (client == null) {
          return null;
        }
    */
    String newappid = project.getPersistentProperty(CloudBeesCorePlugin.PRJ_APPID_KEY);

    if (newappid != null && newappid.length() > 0) {
      return newappid;
    }
    return project.getName();
  }

  private static String getAccountAppId(final String account, final String id,
      final BeesClient client/*, final String warFile*/, IProject project) throws Exception {

    if (id == null || "".equals(id)) {
      String newappid = project.getPersistentProperty(CloudBeesCorePlugin.PRJ_APPID_KEY);
      String newaccount = project.getPersistentProperty(CloudBeesCorePlugin.PRJ_ACCOUNT_KEY);

      if (newappid != null && newappid.length() > 0) {
        String ac = account;
        if (newaccount != null && newaccount.length() > 0) {
          ac = newaccount;
        }
        return ac + "/" + newappid;
      }

      /*      ApplicationConfiguration applicationConfiguration = client.getApplicationConfiguration(warFile, account,
                new String[] {});
            appId = applicationConfiguration.getApplicationId();
      */
    } else {
      return account + "/" + id;
    }
    return account + "/" + project.getName();
  }

  /**
   * @param project
   *          IProject is used for java version detection. If IProject is not known then submit null and workspace java
   *          version will be used.
   * @param accountAppId
   * @param warPath
   * @param monitor
   * @return
   * @throws CloudBeesException
   * @throws CoreException
   * @throws FileNotFoundException
   * @throws Exception
   */
  public static ApplicationDeployArchiveResponse deploy(IProject project, final String accountAppId,
      final File warFile, IProgressMonitor monitor) throws CloudBeesException, CoreException, FileNotFoundException,
      Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient client = getBeesClient(grandCentralService);
    if (client == null) {
      return null;
    }

    String jver = getJavaVersion(project);
    Map<String, String> params = new HashMap<String, String>();
    params.put("runtime.java_version", jver);

    String deployType = getExtension(warFile.getName());

    ApplicationDeployArgs.Builder argBuilder = new ApplicationDeployArgs.Builder(accountAppId)
        .deployPackage(warFile, deployType).withParams(params)
        .withProgressFeedback(new UploadProgressWithMonitor(monitor));

    ApplicationDeployArchiveResponse res = client.applicationDeployArchive(argBuilder.build());

    /*    ApplicationDeployArchiveResponse applicationDeployWar = client.applicationDeployWar(appId, env, null, new File(
            warPath), null, new UploadProgressWithMonitor(monitor));
    */
    update();

    return res;
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

  /**
   * Delete database
   * 
   * @param appId
   * @throws Exception
   */
  public static void deleteDatabase(final String dbId) throws Exception {
    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    BeesClient beesClient = getBeesClient(grandCentralService);
    if (beesClient != null) {
      beesClient.databaseDelete(dbId);
    }
  }

  private static IFile getWarFile(final IProject project, final boolean build, IProgressMonitor monitor)
      throws CloudBeesException, CoreException, FileNotFoundException {

    // First attempt to build through extension points
    for (WarBuilderHook hook : getWarBuilderHooks()) {
      IFile result = hook.buildProject(project, new SubProgressMonitor(monitor, 100));
      if (result != null) {
        return result;
      }
    }

    // Extension-point-based attempts failed, try with ant

    if (build) {
      runTargets(project, new String[] { "dist" });
      project.refreshLocal(IFile.DEPTH_INFINITE, null);
    }

    final IFile latestFile[] = { null };

    try {
      project.accept(new IResourceVisitor() {
        @Override
        public boolean visit(final IResource resource) throws CoreException {
          if (resource.getType() == IResource.FILE && hasSupportedExtension(resource.getName())) {
            if (latestFile[0] == null || resource.getModificationStamp() > latestFile[0].getModificationStamp()) {
              latestFile[0] = (IFile) resource;
            }
          }
          return true;
        }
      });
    } catch (CoreException e) {
      throw new CloudBeesException("Failed to retrieve deployable artifact", e);
    }

    if (latestFile[0] != null) {
      return latestFile[0];
    }

    throw new FileNotFoundException(
        "Could not find .war file in build folder. Ensure that your build.xml has a 'dist' task that generates a war file to build directory.");

  }

  private static IFolder getBuildFolder(final IProject project) throws CloudBeesException, CoreException,
      FileNotFoundException {

    IFolder folder = project.getFolder("build");
    if (!folder.exists()) {
      runTargets(project, new String[] { "dist" });
      project.refreshLocal(IFile.DEPTH_INFINITE, null);

      if (!folder.exists()) {
        throw new FileNotFoundException(
            "Unexpected project structure. Could not find folder \"build\" in project root.");
      }
    }

    return folder;
  }

  private static BeesClient getBeesClient(final GrandCentralService grandCentralService) throws CloudBeesException {
    if (grandCentralService.hasAuthInfo()) {
      AuthInfo cachedAuthInfo = grandCentralService.getCachedAuthInfo(false, new NullProgressMonitor());

      String api_key = cachedAuthInfo.getAuth().api_key;
      String secret_key = cachedAuthInfo.getAuth().secret_key;

      BeesClientConfiguration conf = new BeesClientConfiguration(API_URL, api_key, secret_key, "xml", "1.0");
      BeesClient beesClient = new BeesClient(conf);
      beesClient.setVerbose(false);
      return beesClient;
    } else {
      return null;
    }
  }

  private static void runTargets(final IProject project, final String[] targets) throws CloudBeesException,
      CoreException {
    AntRunner runner = new AntRunner();

    runner.setBuildFileLocation(getBuildXmlPath(project));
    runner.setExecutionTargets(targets);

    GrandCentralService grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
    AuthInfo cachedAuthInfo = grandCentralService.getCachedAuthInfo(false);

    String secretKey = "-Dbees.apiSecret=" + cachedAuthInfo.getAuth().secret_key;//$NON-NLS-1$
    String authKey = " -Dbees.apiKey=" + cachedAuthInfo.getAuth().api_key;//$NON-NLS-1$
    String beesHome = " -Dbees.home=" + CBSdkActivator.getDefault().getBeesHome();
    runner.setArguments(secretKey + authKey + beesHome);

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

  private static List<WarBuilderHook> getWarBuilderHooks() throws CoreException {
    List<WarBuilderHook> hooks = new ArrayList<WarBuilderHook>();

    IExtension[] extensions = Platform.getExtensionRegistry()
        .getExtensionPoint(CBRunCoreActivator.PLUGIN_ID, "warBuilderHook").getExtensions();

    for (IExtension extension : extensions) {
      for (IConfigurationElement element : extension.getConfigurationElements()) {
        Object executableExtension = element.createExecutableExtension("defaultHandler");
        if (executableExtension instanceof WarBuilderHook) {
          hooks.add((WarBuilderHook) executableExtension);
        }
      }
    }

    return hooks;
  }

  public final static boolean hasSupportedExtension(String filename) {
    if (filename == null) {
      return false;
    }
    for (String ext : SUPPORTED_EXTENSIONS) {
      if (filename.endsWith(ext)) {
        return true;
      }
    }
    return false;
  }

  public final static ProcessBuilder createBeesProcess(boolean addAuth, String... cmd) throws CloudBeesException {
    return createBeesProcess(addAuth, new String[] {}, cmd);
  }

  public final static ProcessBuilder createBeesProcess(boolean addAuth, String[] vmargs, String... cmd)
      throws CloudBeesException {

    List<String> cmds = new ArrayList<String>();

    String secretKey = null;
    String authKey = null;

    if (addAuth) {
      GrandCentralService grandCentralService;
      grandCentralService = CloudBeesCorePlugin.getDefault().getGrandCentralService();
      AuthInfo cachedAuthInfo = grandCentralService.getCachedAuthInfo(false);
      secretKey = "-Dbees.apiSecret=" + cachedAuthInfo.getAuth().secret_key;//$NON-NLS-1$
      authKey = "-Dbees.apiKey=" + cachedAuthInfo.getAuth().api_key;//$NON-NLS-1$
    }

    String beesHome = "-Dbees.home=" + CBSdkActivator.getDefault().getBeesHome();
    String beesHomeDir = CBSdkActivator.getDefault().getBeesHome();

    //java.home=C:\Java\jdk1.6.0_29\jre
    //String java = System.getProperty("eclipse.vm");

    //FIXME At one point we might want to use eclipse JRE configs to suport project-specific JREs

    String java = System.getProperty("java.home");
    if (!java.endsWith(File.separator)) {
      java = java + File.separator + "bin" + File.separator + "java";
    }

    String[] c1 = new String[] { java, "-Xmx256m" };

    String[] c2 = new String[] { beesHome, "-cp", beesHomeDir + "lib/cloudbees-boot.jar",
        "com.cloudbees.sdk.boot.Launcher" };

    cmds.addAll(Arrays.asList(c1));
    if (addAuth) {
      cmds.add(secretKey);
      cmds.add(authKey);
    }
    cmds.addAll(Arrays.asList(vmargs));
    cmds.addAll(Arrays.asList(c2));

    if (cmd != null) {
      for (int i = 0; i < cmd.length; i++) {
        String[] subcmd = cmd[i].split(" ");
        if (subcmd!=null && subcmd.length>0) {
          cmds.addAll(Arrays.asList(subcmd));     
        }
      }
    }    

    final ProcessBuilder pb = new ProcessBuilder(cmds);

    pb.environment().put("BEES_HOME", beesHomeDir);
    pb.directory(new File(beesHomeDir));
    pb.redirectErrorStream(true);
    return pb;

  }
}
