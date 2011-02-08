package com.cloudbees.eclipse.ui.views.instances;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;

class InstanceGroup {

  private List<JenkinsInstanceHolder> instances;
  private String name;
  private boolean cloudHosted;

  public InstanceGroup(String name, boolean cloudHosted) {
    instances = new ArrayList<JenkinsInstanceHolder>();
    this.name = name;
    this.cloudHosted = cloudHosted;
  }

  public boolean isCloudHosted() {
    return cloudHosted;
  }

  public String getName() {
    return name;
  }

  public void addChild(JenkinsInstanceResponse child) {
    instances.add(new JenkinsInstanceHolder(this, child));
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
}
