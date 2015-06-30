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
package com.cloudbees.eclipse.core;

import java.util.List;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;

public class CBRemoteChangeAdapter implements CBRemoteChangeListener {

  public void activeJobViewChanged(JenkinsJobsResponse newView) {
  }

  public void jenkinsChanged(List<JenkinsInstanceResponse> instances) {
  }

  public void activeJobHistoryChanged(JenkinsJobAndBuildsResponse newView) {
  }

  public void activeAccountChanged(String email, String newAccountName) {
    
  }

  public void jenkinsStatusUpdate(String viewUrl, boolean online) {
    
  }
  
}
