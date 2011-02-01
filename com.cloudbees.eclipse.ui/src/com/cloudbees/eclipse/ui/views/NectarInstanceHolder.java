package com.cloudbees.eclipse.ui.views;

import org.eclipse.core.runtime.IAdaptable;

import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;

class NectarInstanceHolder implements IAdaptable {
  private InstanceGroup parent;
  private NectarInstanceResponse backing;


  public NectarInstanceHolder(InstanceGroup instanceGroup, NectarInstanceResponse backing) {
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

  public NectarInstanceResponse getBacking() {
    return backing;
  }
}