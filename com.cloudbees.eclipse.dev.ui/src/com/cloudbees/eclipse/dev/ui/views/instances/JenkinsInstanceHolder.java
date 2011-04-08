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