/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.cloudbees.eclipse.dtp.internal;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.cloudbees.api.DatabaseInfo;

public class DatabaseInfoPropertySource implements IPropertySource {

  private static final String BASE = DatabaseInfoPropertySource.class.getSimpleName().toLowerCase();
  private static final String PROPRTY_NAME = BASE + ".id";
  private static final String PROPRTY_MASTER = BASE + ".title";
  private static final String PROPRTY_OWNER = BASE + ".owner";
  private static final String PROPRTY_USERNAME = BASE + ".username";
  private static final String PROPRTY_STATUS = BASE + ".status";
  private static final String PROPRTY_CREATED_DATE = BASE + ".created.date";

  private IPropertyDescriptor[] propertyDescriptors;
  private final DatabaseInfo dbInfo;

  public DatabaseInfoPropertySource(DatabaseInfo dbInfo) {
    this.dbInfo = dbInfo;
  }

  @Override
  public Object getEditableValue() {
    return this;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    if (this.propertyDescriptors == null) {

      PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPRTY_NAME, "Database Name");

      PropertyDescriptor masterDescriptor = new PropertyDescriptor(PROPRTY_MASTER, "Master");
      PropertyDescriptor ownerDescriptor = new PropertyDescriptor(PROPRTY_OWNER, "Owner");
      PropertyDescriptor usernameDescriptor = new PropertyDescriptor(PROPRTY_USERNAME, "Username");

      PropertyDescriptor statusDescriptor = new PropertyDescriptor(PROPRTY_STATUS, "Status");

      PropertyDescriptor createdDateDescriptor = new PropertyDescriptor(PROPRTY_CREATED_DATE, "Created Date");
      createdDateDescriptor.setLabelProvider(new LabelProvider() {
        @Override
        public String getText(Object element) {
          if (element instanceof Date) {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm").format((Date) element);
          } else {
            return super.getText(element);
          }
        }
      });

      this.propertyDescriptors = new IPropertyDescriptor[] { nameDescriptor, ownerDescriptor, usernameDescriptor, masterDescriptor, statusDescriptor,
          createdDateDescriptor};
    }

    return this.propertyDescriptors;
  }

  @Override
  public Object getPropertyValue(Object id) {
    if (id.equals(PROPRTY_NAME)) {
      return this.dbInfo.getName();
    }

    if (id.equals(PROPRTY_MASTER)) {      
      return this.dbInfo.getMaster();
    }

    if (id.equals(PROPRTY_OWNER)) {
      return this.dbInfo.getOwner();
    }

    if (id.equals(PROPRTY_USERNAME)) {
      return this.dbInfo.getUsername();
    }

    if (id.equals(PROPRTY_STATUS)) {
      return this.dbInfo.getStatus();
    }

    if (id.equals(PROPRTY_CREATED_DATE)) {
      return this.dbInfo.getCreated();
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
