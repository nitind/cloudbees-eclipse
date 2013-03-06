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
package com.cloudbees.eclipse.core;

import java.util.List;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;

public interface CBRemoteChangeListener {

  void activeJobViewChanged(JenkinsJobsResponse newView);

  void jenkinsChanged(List<JenkinsInstanceResponse> instances);

  void activeJobHistoryChanged(JenkinsJobAndBuildsResponse newView);

  void forgeChanged(List<ForgeInstance> instances);
  
  void activeAccountChanged(String email, String newAccountName);

  /**
   * Called when jenkins instance status could have been potentially changed. For example called whenever jobs list changed for this url 
   * 
   * @param viewUrl
   * @param online
   */
  void jenkinsStatusUpdate(String viewUrl, boolean online);
  
}
