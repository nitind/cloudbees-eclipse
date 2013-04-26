/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.m2e;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.run.core.WarBuilderHook;

public class MavenWarBuilderHook implements WarBuilderHook {

  public MavenWarBuilderHook() {
  }

  public IFile buildProject(IProject project, IProgressMonitor monitor) throws CloudBeesException {
    return CBMavenUtils.runMaven(project, monitor, new String[] {"package"});
  }

}
