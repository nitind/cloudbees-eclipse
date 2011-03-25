package com.cloudbees.eclipse.run.core;

import org.apache.tools.ant.listener.TimestampedLogger;
import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.GrandCentralService.AuthInfo;

public class TestRunner {

  public void deploy(IProject project) throws Exception {
    runTargets(project, new String[] { "deploy" });
  }

  private void runTargets(IProject project, String[] targets) throws CloudBeesException, CoreException {
    AntRunner runner = new AntRunner();

    runner.setBuildFileLocation(getBuildXmlPath(project));
    runner.setExecutionTargets(targets);

    AuthInfo cachedAuthInfo = CloudBeesCorePlugin.getDefault().getGrandCentralService().getCachedAuthInfo(false);

    String secretKey = "-Dbees.apiSecret=" + cachedAuthInfo.getAuth().secret_key;//$NON-NLS-1$
    String authKey = " -Dbees.apiKey=" + cachedAuthInfo.getAuth().api_key;//$NON-NLS-1$
    runner.setArguments(secretKey + authKey);

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
