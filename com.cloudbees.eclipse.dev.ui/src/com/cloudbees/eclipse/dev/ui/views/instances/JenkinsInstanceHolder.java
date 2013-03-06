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
package com.cloudbees.eclipse.dev.ui.views.instances;

import org.eclipse.core.runtime.IAdaptable;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;

class JenkinsInstanceHolder implements IAdaptable {
  private InstanceGroup parent;
  private JenkinsInstanceResponse backing;


  public JenkinsInstanceHolder(InstanceGroup instanceGroup, JenkinsInstanceResponse backing) {
    this.backing = backing;
    this.parent = instanceGroup;
  }

  public String getName() {
    return backing.nodeName != null ? backing.nodeName : "unnamed";
  }


  public InstanceGroup getParent() {
    return parent;
  }

  public String toString() {
    return getName();
  }

  public Object getAdapter(Class key) {
    return null;
  }

  public JenkinsInstanceResponse getBacking() {
    return backing;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((backing == null) ? 0 : backing.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof JenkinsInstanceHolder)) {
      return false;
    }
    JenkinsInstanceHolder other = (JenkinsInstanceHolder) obj;
    if (backing == null) {
      if (other.backing != null) {
        return false;
      }
    } else if (!backing.equals(other.backing)) {
      return false;
    }
    return true;
  }
}