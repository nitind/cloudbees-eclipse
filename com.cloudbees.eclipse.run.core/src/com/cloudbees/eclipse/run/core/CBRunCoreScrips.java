package com.cloudbees.eclipse.run.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

public class CBRunCoreScrips {
  
  /**
   * Copies the sample-webapp project to user workspace directory 
   * 
   * @param workspacePath user workspace path
   * @param projectName name of the project
   * @throws IOException
   * @throws CoreException
   */
  public static void executeCopySampleWebAppScript(String workspacePath, String projectName) throws IOException, CoreException {

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("workspacePath", workspacePath);
    properties.put("projectName", projectName);
    
    Bundle bundle = CBRunCoreActivator.getContext().getBundle();
    Path path = new Path("scripts/copy-project.xml");
    String scriptLocation = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();
    
    AntRunner antRunner = new AntRunner();
    antRunner.setBuildFileLocation(scriptLocation);
    antRunner.setExecutionTargets(new String [] { "copy-sample-webapp" });
    antRunner.addUserProperties(properties);
    
    antRunner.run();
  }
  
}