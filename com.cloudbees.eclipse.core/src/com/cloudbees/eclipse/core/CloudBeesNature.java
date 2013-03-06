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
package com.cloudbees.eclipse.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class CloudBeesNature implements IProjectNature {

  public static final String NATURE_ID = CloudBeesCorePlugin.PLUGIN_ID + ".cloudbeesNature";

  private IProject project;

  //   public static final String BUILDER_ID = "com.cloudbees.eclipse.core.builder.cloudbeesBuilder";

  public CloudBeesNature() {
  }

  public static boolean isEnabledFor(final IResource resource) {
    return NatureUtil.isEnabledFor(resource, NATURE_ID);
  }

  public void configure() throws CoreException {
    //    IProjectDescription desc = this.project.getDescription();
    //    ICommand[] commands = desc.getBuildSpec();
    //
    //    for (int i = 0; i < commands.length; ++i) {
    //      if (commands[i].getBuilderName().equals(BUILDER_ID)) {
    //        return;
    //      }
    //    }
    //
    //    ICommand[] newCommands = new ICommand[commands.length + 1];
    //    System.arraycopy(commands, 0, newCommands, 0, commands.length);
    //    ICommand command = desc.newCommand();
    //    command.setBuilderName(BUILDER_ID);
    //    newCommands[newCommands.length - 1] = command;
    //    desc.setBuildSpec(newCommands);
    //    this.project.setDescription(desc, null);
  }

  public void deconfigure() throws CoreException {
    //    IProjectDescription description = getProject().getDescription();
    //    ICommand[] commands = description.getBuildSpec();
    //    for (int i = 0; i < commands.length; ++i) {
    //      if (commands[i].getBuilderName().equals(BUILDER_ID)) {
    //        ICommand[] newCommands = new ICommand[commands.length - 1];
    //        System.arraycopy(commands, 0, newCommands, 0, i);
    //        System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
    //        description.setBuildSpec(newCommands);
    //        return;
    //      }
    //    }
  }

  public IProject getProject() {
    return this.project;
  }

  public void setProject(final IProject project) {
    this.project = project;
  }

}
