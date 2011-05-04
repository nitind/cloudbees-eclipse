package com.cloudbees.eclipse.dev.ui.views.instances;

import java.util.ArrayList;
import java.util.List;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;

public class ForgeGroup {

  private List<ForgeInstance> instances = new ArrayList<ForgeInstance>();
  private String name;
  private boolean loading;

  public ForgeGroup(final String name) {
    this.name = name;
    this.loading = true;
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

  public void addChild(final ForgeInstance child) {
    this.instances.add(child);
  }

  public ForgeInstance[] getChildren() {
    return this.instances.toArray(new ForgeInstance[this.instances.size()]);
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
    if (!(obj instanceof ForgeGroup)) {
      return false;
    }
    ForgeGroup other = (ForgeGroup) obj;
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
