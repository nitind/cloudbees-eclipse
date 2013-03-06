/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
