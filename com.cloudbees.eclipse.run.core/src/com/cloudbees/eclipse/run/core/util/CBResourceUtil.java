package com.cloudbees.eclipse.run.core.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import com.cloudbees.eclipse.core.CloudBeesNature;

public class CBResourceUtil {
  
  /**
   * @return projects in workbench that have {@link CloudBeesNature}
   */
  public static List<IProject> getWorkbenchCloudBeesProjects() {
    List<IProject> projects = new ArrayList<IProject>();
    for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
      if (CloudBeesNature.isEnabledFor(project)) {
        projects.add(project);
      }
    }
    return projects;
  }
  
}
