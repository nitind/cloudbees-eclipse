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

public interface CBRemoteChangeListener {

  void activeJobViewChanged(JenkinsJobsResponse newView);

  void jenkinsChanged(List<JenkinsInstanceResponse> instances);

  void activeJobHistoryChanged(JenkinsJobAndBuildsResponse newView);

  void activeAccountChanged(String email, String newAccountName);

  /**
   * Called when jenkins instance status could have been potentially changed. For example called whenever jobs list changed for this url 
   * 
   * @param viewUrl
   * @param online
   */
  void jenkinsStatusUpdate(String viewUrl, boolean online);
  
}
