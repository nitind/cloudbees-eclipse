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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobAndBuildsResponse;

public class BuildHistoryContentProvider implements IStructuredContentProvider {

  private JenkinsJobAndBuildsResponse root;

  public BuildHistoryContentProvider() {
  }

  public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
    if (newInput instanceof JenkinsJobAndBuildsResponse) {
      this.root = (JenkinsJobAndBuildsResponse) newInput;
    } else {
      this.root = null; // reset
    }
  }

  public void dispose() {
  }

  public Object[] getElements(final Object parent) {
    if (parent instanceof IViewSite) {
      if (this.root == null) {
        return new JenkinsBuild[0];
      } else {
        return this.root.builds;
      }
    }
    return null;
  }
}
