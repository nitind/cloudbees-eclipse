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
package com.cloudbees.eclipse.dev.ui.views.instances;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;

public class InstancePropertySource implements IPropertySource {

  private static final String BASE = InstancePropertySource.class.getSimpleName().toLowerCase();
  private static final Object PROPERTY_AT_CLOUD = BASE + ".atCloud";
  private static final Object PROPERTY_DESCRIPTION = BASE + ".description";
  private static final Object PROPERTY_LABEL = BASE + ".label";
  private static final Object PROPERTY_NODE_DESCRIPTION = BASE + ".node.description";
  private static final Object PROPERTY_NODE_NAME = BASE + ".node.name";
  private static final Object PROPERTY_OFFLINE = BASE + ".offline";
  private static final Object PROPERTY_VIERW_URL = BASE + ".view.url";

  private IPropertyDescriptor[] propertyDescriptors;
  private final JenkinsInstanceResponse jenkinsInstance;

  public InstancePropertySource(JenkinsInstanceResponse appInfo) {
    this.jenkinsInstance = appInfo;
  }

  @Override
  public Object getEditableValue() {
    return this;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    if (this.propertyDescriptors == null) {

      PropertyDescriptor labelDescriptor = new PropertyDescriptor(PROPERTY_LABEL, "Label");
      PropertyDescriptor descrDescriptor = new PropertyDescriptor(PROPERTY_DESCRIPTION, "Description");
      PropertyDescriptor atCloudDescriptor = new PropertyDescriptor(PROPERTY_AT_CLOUD, "Hosted at DEV@Cloud");
      PropertyDescriptor nodeNameDescriptor = new PropertyDescriptor(PROPERTY_NODE_NAME, "Node Name");
      PropertyDescriptor nodeDescrDescriptor = new PropertyDescriptor(PROPERTY_NODE_DESCRIPTION, "Node Description");
      PropertyDescriptor offlineDescriptor = new PropertyDescriptor(PROPERTY_OFFLINE, "Offline");
      PropertyDescriptor viewUrlDescriptor = new PropertyDescriptor(PROPERTY_VIERW_URL, "View URL");

      this.propertyDescriptors = new IPropertyDescriptor[] { labelDescriptor, descrDescriptor, atCloudDescriptor,
          nodeNameDescriptor, nodeDescrDescriptor, offlineDescriptor, viewUrlDescriptor };
    }

    return this.propertyDescriptors;
  }

  @Override
  public Object getPropertyValue(Object id) {
    if (id.equals(PROPERTY_LABEL)) {
      return this.jenkinsInstance.label;
    }
    if (id.equals(PROPERTY_DESCRIPTION)) {
      return this.jenkinsInstance.description;
    }
    if (id.equals(PROPERTY_AT_CLOUD)) {
      return this.jenkinsInstance.atCloud;
    }
    if (id.equals(PROPERTY_NODE_NAME)) {
      return this.jenkinsInstance.nodeName;
    }
    if (id.equals(PROPERTY_NODE_DESCRIPTION)) {
      return this.jenkinsInstance.nodeDescription;
    }
    if (id.equals(PROPERTY_OFFLINE)) {
      return this.jenkinsInstance.offline;
    }
    if (id.equals(PROPERTY_VIERW_URL)) {
      return this.jenkinsInstance.viewUrl;
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
