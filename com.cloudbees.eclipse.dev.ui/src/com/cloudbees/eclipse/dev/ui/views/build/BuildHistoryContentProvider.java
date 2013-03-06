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
