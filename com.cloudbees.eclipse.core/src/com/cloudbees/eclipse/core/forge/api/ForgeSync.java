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
package com.cloudbees.eclipse.core.forge.api;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsScmConfig;

public interface ForgeSync {

  void updateStatus(ForgeInstance instance, IProgressMonitor subProgressMonitor) throws CloudBeesException;

  void sync(ForgeInstance instance, IProgressMonitor monitor) throws CloudBeesException;

  boolean openRemoteFile(JenkinsScmConfig scmConfig, ChangeSetPathItem item, IProgressMonitor monitor);

  void addToRepository(ForgeInstance instance, IProject project, IProgressMonitor monitor) throws CloudBeesException;

  boolean isUnderSvnScm(IProject project);

  ForgeInstance getMainRepo(IProject project);

}
