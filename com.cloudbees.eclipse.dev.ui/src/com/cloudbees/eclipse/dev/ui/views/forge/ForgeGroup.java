/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.dev.ui.views.forge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.ui.IActionFilter;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.ui.views.ICBGroup;

public class ForgeGroup implements ICBGroup {

  private List<ForgeInstance> instances = new ArrayList<ForgeInstance>();
  private String name;
  private boolean loading;
  
  private IActionFilter af = new IActionFilter() {
    public boolean testAttribute(Object target, String name, String value) {
      if (name.equals("typeName")) {
        ForgeInstance fi = (ForgeInstance) target;
        return fi.getTypeName().equals(value);
      }
      if (name.equals("statusName")) {
        ForgeInstance fi = (ForgeInstance) target;
        return fi.status.name().equals(value);
      }
      return false;
    }
  };

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
    // ensure child is adaptable to IActionFilter
    child.addAdaptable(IActionFilter.class, af);
    this.instances.add(child);
  }

  public ForgeInstance[] getChildren() {
    ForgeInstance[] ret = this.instances.toArray(new ForgeInstance[this.instances.size()]);
    return ret;
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

  @Override
  public int getOrder() {
    return 5;
  }
}
