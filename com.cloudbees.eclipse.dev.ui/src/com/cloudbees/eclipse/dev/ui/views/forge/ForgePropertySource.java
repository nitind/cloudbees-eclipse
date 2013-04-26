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

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;

public class ForgePropertySource implements IPropertySource {

  private static final String BASE = ForgePropertySource.class.getSimpleName().toLowerCase();
  private static final Object PROPERTY_TYPE = BASE + ".type";
  private static final Object PROPERTY_URL = BASE + ".url";
  private static final Object PROPERTY_STATUS = BASE + ".status";

  private IPropertyDescriptor[] propertyDescriptors;
  private final ForgeInstance forge;

  public ForgePropertySource(ForgeInstance forge) {
    this.forge = forge;
  }

  @Override
  public Object getEditableValue() {
    return this;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    if (this.propertyDescriptors == null) {

      PropertyDescriptor typeDescriptor = new PropertyDescriptor(PROPERTY_TYPE, "Type");
      PropertyDescriptor statusDescriptor = new PropertyDescriptor(PROPERTY_STATUS, "Status");
      PropertyDescriptor urlDescriptor = new PropertyDescriptor(PROPERTY_URL, "URL");

      this.propertyDescriptors = new IPropertyDescriptor[] { typeDescriptor, statusDescriptor, urlDescriptor };
    }

    return this.propertyDescriptors;
  }

  @Override
  public Object getPropertyValue(Object id) {
    if (id.equals(PROPERTY_TYPE)) {
      return this.forge.type.name();
    }
    if (id.equals(PROPERTY_STATUS)) {
      return this.forge.status.name();
    }
    if (id.equals(PROPERTY_URL)) {
      return this.forge.url;
    }
    return null;
  }

  @Override
  public boolean isPropertySet(Object id) {
    return false;
  }

  @Override
  public void resetPropertyValue(Object id) {
  }

  @Override
  public void setPropertyValue(Object id, Object value) {
  }

}
