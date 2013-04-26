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

/**
 * Main response object for the job view For more detailed test build a new class for
 * /.../job/.../391/testReport/api/json
 * ?tree=duration,failCount,passCount,skipCount,suites[cases[className,duration,name,status]]
 *
 * @author ahti
 */
public class JenkinsBuildDetailsResponse extends BaseJenkinsResponse {

  @Expose(deserialize = false, serialize = false)
  public final static String QTREE = QTreeFactory.create(JenkinsBuildDetailsResponse.class);

  public Action[] actions;

  public Artifact[] artifacts;

  public Long duration;

  public Boolean building;
  public String description;
  public String name;
  public String displayName;
  public String fullDisplayName;
  public String id;
  //public Boolean keepLog;
  public long number;
  public String result; // SUCCESS
  public Long timestamp;
  public String url;
  public String builtOn;

  public ChangeSet changeSet;

  public Author[] culprits;

  public static class Action {

    // Can be null
    public Cause[] causes;

    // Can be null. junit
    public Long failCount;
    public Long skipCount;
    public Long totalCount;
    public String urlName;

    public static class Cause {
      public String shortDescription;
    }
  }

  public static class ChangeSet {

    public ChangeSetItem[] items;
    public String kind; // hg

    public static class ChangeSetItem {
      public String id;
      public String[] addedPaths;
      public Author author;
      public String date;
      public String[] deletedPaths;
      public String[] modifiedPaths;
      public Boolean merge;
      public String msg;
      public String comment;
      //public String node;
      public ChangePath[] paths;
      public long rev;

      public static class ChangePath {
        public String editType;
        public String file;
      }
    }
  }

  public static class Author {
    public String absoluteUrl;
    public String fullName;
  }

  public static class Artifact {
    public String displayPath;
    public String fileName;
    public String relativePath;
  }

  public String getDisplayName() {
    String result = this.fullDisplayName;
    if (result == null) {
      result = this.displayName;
      if (result == null) {
        result = this.name;
      }

      if (result != null && result.indexOf("#") < 0) {
        result += " #" + this.number;
      }
    }
    return result;
  }
}
