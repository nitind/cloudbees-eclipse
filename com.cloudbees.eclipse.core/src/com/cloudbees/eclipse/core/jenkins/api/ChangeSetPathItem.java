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

import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem;

public class ChangeSetPathItem {
  public enum TYPE {
    ADDED, DELETED, MODIFIED
  };

  public TYPE type;
  public String path;
  public ChangeSetItem parent;

  public ChangeSetPathItem(final ChangeSetItem parent, final TYPE type, final String string) {
    this.type = type;
    this.path = string;
    this.parent = parent;
  }
}
