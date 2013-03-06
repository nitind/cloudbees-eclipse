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
package com.cloudbees.eclipse.run.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ant.core.AntRunner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

public class CBRunCoreScripts {

  /**
   * Copies the sample-webapp project to user workspace directory
   * 
   * @param workspacePath
   *          user workspace path
   * @param projectName
   *          name of the project
   * @throws IOException
   * @throws CoreException
   */
  public static void executeCopySampleWebAppScript(String workspacePath, String projectName) throws IOException,
      CoreException {

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("workspacePath", workspacePath);
    properties.put("projectName", projectName);

    Bundle bundle = CBRunCoreActivator.getContext().getBundle();
    Path path = new Path("scripts/copy-project.xml");
    String scriptLocation = FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile();

    AntRunner antRunner = new AntRunner();
    antRunner.setBuildFileLocation(scriptLocation);
    antRunner.setExecutionTargets(new String[] { "copy-sample-webapp" });
    antRunner.addUserProperties(properties);

    antRunner.run();
  }

}
