package com.cloudbees.eclipse.run.core;

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

public class CBRunCoreScrips {

  public static IStatus executeCopySampleWebAppScript(String workspacePath, String projectName) {

    IStatus result = null;

    try {
      Bundle bundle = CBRunCoreActivator.getContext().getBundle();
      Path path = new Path("scripts/copy-project.xml");
      File antFile = new File(FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile());

      Project project = new Project();
      project.setUserProperty("ant.file", antFile.getAbsolutePath());
      project.init();

      ProjectHelper helper = ProjectHelper.getProjectHelper();
      project.addReference("ant.projectHelper", helper);
      helper.parse(project, antFile);

      project.setProperty("workspacePath", workspacePath);
      project.setProperty("projectName", projectName);
      project.executeTarget(project.getDefaultTarget());

      // TODO status

    } catch (Exception e) {
      // TODO status
      e.printStackTrace();
    }

    return result;
  }

}
