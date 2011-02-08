package com.cloudbees.eclipse.ui.views.instances;

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
}