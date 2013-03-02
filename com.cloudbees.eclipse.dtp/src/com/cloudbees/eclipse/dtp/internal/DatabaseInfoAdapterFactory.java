package com.cloudbees.eclipse.dtp.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.views.properties.IPropertySource;

import com.cloudbees.api.DatabaseInfo;

public class DatabaseInfoAdapterFactory implements IAdapterFactory {

  @Override
  public Object getAdapter(Object adaptableObject, Class adapterType) {

    if (!(adaptableObject instanceof DatabaseInfo)) {
      return null;
    }

    if (adapterType == IPropertySource.class) {
      return new DatabaseInfoPropertySource((DatabaseInfo) adaptableObject);
    }

    if (adapterType == IActionFilter.class) {
      return new DatabaseStatusActionFilter();
    }

    return null;
  }

  @Override
  public Class[] getAdapterList() {
    return new Class[] { IPropertySource.class, IActionFilter.class };
  }

}
