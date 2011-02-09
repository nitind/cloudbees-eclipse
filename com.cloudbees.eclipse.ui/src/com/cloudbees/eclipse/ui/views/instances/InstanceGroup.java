package com.cloudbees.eclipse.ui.views.instances;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;

class InstanceGroup {

  private List<JenkinsInstanceHolder> instances;
  private String name;
  private boolean cloudHosted;
  private boolean loading;

  public InstanceGroup(String name, boolean cloudHosted) {
    this.instances = new ArrayList<JenkinsInstanceHolder>();
    this.name = name;
    this.cloudHosted = cloudHosted;
    this.loading = true;
  }

  public boolean isCloudHosted() {
    return cloudHosted;
  }

  public String getName() {
    String result = name;
    if (loading) {
      result += " (loading)";
    }
    return result;
  }

  public void setLoading(boolean loading) {
    this.loading = loading;
  }

  public void addChild(JenkinsInstanceResponse child) {
    this.instances.add(new JenkinsInstanceHolder(this, child));
  }

  public JenkinsInstanceResponse[] getChildren() {
    List<JenkinsInstanceResponse> resp = new ArrayList<JenkinsInstanceResponse>();
    //build view list    
    Iterator<JenkinsInstanceHolder> it = instances.iterator();
    while (it.hasNext()) {
      JenkinsInstanceHolder holder = (JenkinsInstanceHolder) it.next();
      resp.add(holder.getBacking());
    }

    return (JenkinsInstanceResponse[]) resp.toArray(new JenkinsInstanceResponse[resp.size()]);
  }

  public boolean hasChildren() {
    return instances.size() > 0;
  }

  public void clear() {
    instances.clear();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    if (!(obj instanceof InstanceGroup)) {
      return false;
    }
    InstanceGroup other = (InstanceGroup) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

}
