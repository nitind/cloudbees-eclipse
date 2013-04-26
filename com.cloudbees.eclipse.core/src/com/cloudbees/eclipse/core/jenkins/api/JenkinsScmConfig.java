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
package com.cloudbees.eclipse.core.jenkins.api;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;

public class JenkinsScmConfig {

  public Repository[] repos;

  public static class Repository {
    public ForgeInstance.TYPE type;
    public String url;
    public String[] branches;
  }

}
