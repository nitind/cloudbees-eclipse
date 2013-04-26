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
package com.cloudbees.eclipse.run.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

final class AppContentProvider implements ITreeContentProvider {

  AppGroup appGroup = new AppGroup("Applications");
  ApplicationListResponse data;

  @Override
  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    if (newInput instanceof ApplicationListResponse) {
      this.data = (ApplicationListResponse) newInput;
    } else {
      this.data = null;
    }
  }

  @Override
  public void dispose() {
  }

  @Override
  public Object[] getElements(final Object element) {
    return getChildren(element);
  }

  @Override
  public Object[] getChildren(final Object element) {
    if (element instanceof IViewSite) {
      return new Object[] { this.appGroup };
    }
    if (this.data != null && element instanceof AppGroup) {
      String activeAccount = null;
      try {
        activeAccount = CloudBeesUIPlugin.getDefault().getActiveAccountName(new NullProgressMonitor());
      } catch (CloudBeesException e) {
      }
      if (activeAccount==null || activeAccount.length()==0) {
        return new ApplicationInfo[0];
      }
      List<ApplicationInfo> arr = this.data.getApplications();
      Iterator<ApplicationInfo> it = arr.iterator();
      List<ApplicationInfo> resList = new ArrayList<ApplicationInfo>();
      while (it.hasNext()) {
        ApplicationInfo applicationInfo = (ApplicationInfo) it.next();
        String id = applicationInfo.getId();
        if (id!=null && id.startsWith(activeAccount+"/")) {
          resList.add(applicationInfo);
        }
      }
      return resList.toArray(new ApplicationInfo[0]);
      //return getAdapters(this.data.getApplications());
    }

    //    if (inputElement instanceof ApplicationListResponse) {
    //      return ((ApplicationListResponse) inputElement).getApplications().toArray();
    //    }
    return null;
  }

  @Override
  public Object getParent(final Object element) {
    return null;
  }

  @Override
  public boolean hasChildren(final Object element) {
    Object[] children = getChildren(element);
    return children != null && children.length > 0;
  }

  //  private Object[] getAdapters(List<ApplicationInfo> list) {
  //    List<ApplicationInfoAdaptable> res = new ArrayList<ApplicationInfoAdaptable>();
  //    for (ApplicationInfo appInfo : list) {
  //      res.add(new ApplicationInfoAdaptable(appInfo));
  //    }
  //    return res.toArray();
  //  }

}
