package com.cloudbees.eclipse.ui.views.nectartree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;

class InstanceGroup {

  private List<NectarInstanceHolder> instances;
  private String name;
  private boolean cloudHosted;

  public InstanceGroup(String name, boolean cloudHosted) {
    instances = new ArrayList<NectarInstanceHolder>();
    this.name = name;
    this.cloudHosted = cloudHosted;
  }

  public boolean isCloudHosted() {
    return cloudHosted;
  }

  public String getName() {
    return name;
  }

  public void addChild(NectarInstanceResponse child) {
    instances.add(new NectarInstanceHolder(this, child));
  }

  public NectarInstanceResponse[] getChildren() {
    List<NectarInstanceResponse> resp = new ArrayList<NectarInstanceResponse>();
    //build view list    
    Iterator<NectarInstanceHolder> it = instances.iterator();
    while (it.hasNext()) {
      NectarInstanceHolder holder = (NectarInstanceHolder) it.next();
      resp.add(holder.getBacking());
    }

    return (NectarInstanceResponse[]) resp.toArray(new NectarInstanceResponse[resp.size()]);
  }

  public boolean hasChildren() {
    return instances.size() > 0;
  }
}
