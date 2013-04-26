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
package com.cloudbees.eclipse.core.gc.api;


import com.google.gson.annotations.SerializedName;

public class ClickStartTemplate extends ClickStartResponseBase {
  
  public String id;
  public String name;
  public String icon;
  public String description;
  public Component[] components;
  
  @SerializedName("doc-url") public String docUrl;

  public static class Component {
    public String name;
    public String icon;
    public String description;
    public String url;
    public String managementUrl;
    public String key;
  }
  
  public String toString() {
    return name;
  }
}
