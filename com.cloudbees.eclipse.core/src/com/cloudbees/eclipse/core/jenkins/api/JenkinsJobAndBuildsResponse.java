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

import com.google.gson.annotations.Expose;

public class JenkinsJobAndBuildsResponse extends BaseJenkinsResponse {

  @Expose(deserialize = false, serialize = false)
  public final static String QTREE = QTreeFactory.create(JenkinsJobAndBuildsResponse.class);

  public String name;
  public Boolean inQueue;

  public JenkinsBuild[] builds;
  public Boolean buildable;
  public String color;
  public HealthReport[] healthReport;
  public JenkinsJobProperty[] property;

}
