package com.cloudbees.eclipse.run.ui.wizards;

import org.apache.maven.cli.MavenCli;
import org.eclipse.core.resources.IProject;

public class CBMavenBuilder {

  public static int buildMavenProject(IProject project) {
    MavenCli cli = new MavenCli();
    return cli.doMain(new String[]{"eclipse:eclipse"},
            project.getFullPath().toFile().getAbsolutePath(),
            System.out, System.out);
  }
  
}
