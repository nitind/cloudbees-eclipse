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
package com.cloudbees.eclipse.dev.ui.views.build;

import org.eclipse.ui.internal.part.NullEditorInput;

public class BuildEditorInput extends NullEditorInput {

  private String buildUrl;
  private String displayName;

  public BuildEditorInput(final String displayName, final String buildUrl) {
    super();
    //CloudBeesUIPlugin.getDefault().getLogger().info("Creating job details editor for url " + job.url);

    this.displayName = displayName;
    this.buildUrl = buildUrl;
  }

  @Override
  public boolean exists() {
    return true;
  }

  public void setBuildUrl(final String buildUrl) {
    this.buildUrl = buildUrl;
  }

  public String getBuildUrl() {
    return this.buildUrl;
  }

  public String getJobUrl() {
    if (this.buildUrl == null) {
      return null;
    }
    String jobUrl = getJobUrl(this.buildUrl);
    return jobUrl;
  }

  public static String getJobUrl(final String buildUrl) {
    String jobUrl = buildUrl;
    jobUrl = jobUrl.trim();
    if (jobUrl.endsWith("/")) {
      jobUrl = jobUrl.substring(0, jobUrl.length() - 1);
    }

    try {
      int pos = jobUrl.lastIndexOf('/');
      @SuppressWarnings("unused")
      long buildNr = Long.parseLong(jobUrl.substring(pos + 1, jobUrl.length()));
      jobUrl = jobUrl.substring(0, pos); // strip build number
    } catch (NumberFormatException e) {
      // not a build number, let's don't strip
    }
    return jobUrl;
  }

  public String getDisplayName() {
    return this.displayName;
  }

  public void setDisplayName(final String displayName) {
    this.displayName = displayName;
  }
}
