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
