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
