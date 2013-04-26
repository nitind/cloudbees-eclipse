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

public class JenkinsInstanceResponse extends BaseJenkinsResponse {

  public String mode; // NORMAL
  public String nodeDescription;
  public String nodeName;
  public String description;
  public Boolean useSecurity;
  public boolean atCloud;

  public PrimaryView primaryView;
  public View[] views;

  @Expose(deserialize = false, serialize = false)
  public boolean offline = false;

  @Expose(deserialize = false, serialize = false)
  public String label;

  @Expose(deserialize = false, serialize = false)
  public final static String QTREE = QTreeFactory.create(JenkinsInstanceResponse.class);

  public JenkinsInstanceResponse() {

  }

  public static class PrimaryView {
    public String name;
    public String url;
  }

  public static class View {
    public String name;
    public String url;
    public String description;

    @Expose(deserialize = false, serialize = false)
    public boolean isPrimary;

    @Expose(deserialize = false, serialize = false)
    public JenkinsInstanceResponse response;

    //public Job[] jobs;
  }

}
