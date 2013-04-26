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
  private static final String PROPERTY_NAME = BASE + ".id";
  private static final String PROPERTY_MASTER = BASE + ".title";
  private static final String PROPERTY_OWNER = BASE + ".owner";
  private static final String PROPERTY_USERNAME = BASE + ".username";
  private static final String PROPERTY_STATUS = BASE + ".status";
  private static final String PROPERTY_CREATED_DATE = BASE + ".created.date";
  //private static final String PROPERTY_PASSWORD = BASE + ".password";

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

      PropertyDescriptor nameDescriptor = new PropertyDescriptor(PROPERTY_NAME, "Database Name");

      PropertyDescriptor masterDescriptor = new PropertyDescriptor(PROPERTY_MASTER, "Master");
      PropertyDescriptor ownerDescriptor = new PropertyDescriptor(PROPERTY_OWNER, "Owner");
      PropertyDescriptor usernameDescriptor = new PropertyDescriptor(PROPERTY_USERNAME, "Username");

      PropertyDescriptor statusDescriptor = new PropertyDescriptor(PROPERTY_STATUS, "Status");

      PropertyDescriptor createdDateDescriptor = new PropertyDescriptor(PROPERTY_CREATED_DATE, "Created Date");
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

/*      PropertyDescriptor passwordDescriptor = new PropertyDescriptor(PROPERTY_PASSWORD, "Password") {
        
        @Override
        public CellEditor createPropertyEditor(Composite parent) {
          return new TextCellEditor(parent) {
            
          };
        }
      };     
      
      passwordDescriptor.setLabelProvider(new LabelProvider() {
        public String getText(Object element) {
          return "..." + element;
        }
      });
*/
      
      this.propertyDescriptors = new IPropertyDescriptor[] { nameDescriptor, ownerDescriptor, usernameDescriptor, masterDescriptor, statusDescriptor,
          createdDateDescriptor/*, passwordDescriptor*/};
    }

    return this.propertyDescriptors;
  }

  @Override
  public Object getPropertyValue(Object id) {
    if (id.equals(PROPERTY_NAME)) {
      return this.dbInfo.getName();
    }

    if (id.equals(PROPERTY_MASTER)) {      
      return this.dbInfo.getMaster();
    }

    if (id.equals(PROPERTY_OWNER)) {
      return this.dbInfo.getOwner();
    }

    if (id.equals(PROPERTY_USERNAME)) {
      return this.dbInfo.getUsername();
    }

    if (id.equals(PROPERTY_STATUS)) {
      return this.dbInfo.getStatus();
    }

    if (id.equals(PROPERTY_CREATED_DATE)) {
      return this.dbInfo.getCreated();
    }

/*    if (id.equals(PROPERTY_PASSWORD)) {
      return this.dbInfo.getPassword();
    }
*/
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
