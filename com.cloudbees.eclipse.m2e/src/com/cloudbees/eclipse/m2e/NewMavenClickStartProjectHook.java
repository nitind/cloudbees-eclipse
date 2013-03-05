package com.cloudbees.eclipse.m2e;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.gc.api.ClickStartCreateResponse;
import com.cloudbees.eclipse.run.core.NewClickStartProjectHook;

public class NewMavenClickStartProjectHook implements NewClickStartProjectHook {

  public NewMavenClickStartProjectHook() {
  }

  @Override
  public void hookProject(ClickStartCreateResponse resp, IProject project, IProgressMonitor monitor) throws CloudBeesException {
    System.out.println("Configuring maven hook! for "+resp);
    boolean mvnExists = project.exists(new Path("/pom.xml"));
    boolean antExists = project.exists(new Path("/build.xml"));

    if (mvnExists) {
      //NatureUtil.addNatures(project, new String[] { "org.eclipse.m2e.core.maven2Nature" }, monitor);
      monitor.subTask("Detected maven build scripts, building project to generate eclipse settings.");
      //int res = CBMavenBuilder.buildMavenProject(project);
      //System.out.println("Maven builder returned: "+res);
      try {
        project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
      } catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

}
