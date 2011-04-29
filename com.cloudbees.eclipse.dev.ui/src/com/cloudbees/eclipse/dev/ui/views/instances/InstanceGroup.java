package com.cloudbees.eclipse.dev.ui.views.instances;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;

public class InstanceGroup {

  private List<JenkinsInstanceHolder> instances;
  private String name;
  private boolean cloudHosted;
  private boolean loading;

  public InstanceGroup(final String name, final boolean cloudHosted) {
    this.instances = new ArrayList<JenkinsInstanceHolder>();
    this.name = name;
    this.cloudHosted = cloudHosted;
    this.loading = true;
  }

  public boolean isCloudHosted() {
    return this.cloudHosted;
  }

  public String getName() {
    return this.name;
  }

  public void setLoading(final boolean loading) {
    this.loading = loading;
  }

  public boolean isLoading() {
    return this.loading;
  }

  public void addChild(final JenkinsInstanceResponse child) {
    this.instances.add(new JenkinsInstanceHolder(this, child));
  }

  public JenkinsInstanceResponse[] getChildren() {
    List<JenkinsInstanceResponse> resp = new ArrayList<JenkinsInstanceResponse>();
    //build view list
    Iterator<JenkinsInstanceHolder> it = this.instances.iterator();
    while (it.hasNext()) {
      JenkinsInstanceHolder holder = it.next();
      resp.add(holder.getBacking());
    }

    return resp.toArray(new JenkinsInstanceResponse[resp.size()]);
  }

  public boolean hasChildren() {
    return this.instances.size() > 0;
  }

  public void clear() {
    this.instances.clear();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
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
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }
}
