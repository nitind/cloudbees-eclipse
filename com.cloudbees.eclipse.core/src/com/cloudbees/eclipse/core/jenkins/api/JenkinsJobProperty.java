/*******************************************************************************
 * Copyright (c) 2013, 2019 Cloud Bees, Inc. and others
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 * 	IBM Corp. - Support choice parameters
 *******************************************************************************/
package com.cloudbees.eclipse.core.jenkins.api;

public class JenkinsJobProperty {

  public ParameterDefinition[] parameterDefinitions;

  public static class ParameterDefinition {
    public String name;
    public String description;
    public String type;
    public ParameterValue defaultParameterValue;
    public String[] choices;
  }

  public static class ParameterValue {
    public String name;
    public String value;
  }
}
