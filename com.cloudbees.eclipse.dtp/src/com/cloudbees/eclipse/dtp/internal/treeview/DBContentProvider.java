package com.cloudbees.eclipse.dtp.internal.treeview;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.api.DatabaseListResponse;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

final class DBContentProvider implements ITreeContentProvider {

  DBGroup dbGroup = new DBGroup("Databases");
  DatabaseListResponse data;

  @Override
  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    if (newInput instanceof DatabaseListResponse) {
      this.data = (DatabaseListResponse) newInput;
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
      return new Object[] { this.dbGroup };
    }
    if (this.data != null && element instanceof DBGroup) {
      String activeAccount = null;
      try {
        activeAccount = CloudBeesUIPlugin.getDefault().getActiveAccountName(new NullProgressMonitor());
      } catch (CloudBeesException e) {
        e.printStackTrace();
      }
      if (activeAccount==null || activeAccount.length()==0) {
        return new DatabaseInfo[0];
      }
      List<DatabaseInfo> arr = this.data.getDatabases();
      Iterator<DatabaseInfo> it = arr.iterator();
      List<DatabaseInfo> resList = new ArrayList<DatabaseInfo>();
      while (it.hasNext()) {
        DatabaseInfo databaseInfo = (DatabaseInfo) it.next();
        String id = databaseInfo.getOwner();
        
        if (id!=null && id.equalsIgnoreCase(activeAccount))
        {
          resList.add(databaseInfo);
        }
      }
      return resList.toArray(new DatabaseInfo[0]);
      //return getAdapters(this.data.getApplications());
    }

    //    if (inputElement instanceof DatabaseListResponse) {
    //      return ((DatabaseListResponse) inputElement).getApplications().toArray();
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

  //  private Object[] getAdapters(List<DatabaseInfo> list) {
  //    List<DatabaseInfoAdaptable> res = new ArrayList<DatabaseInfoAdaptable>();
  //    for (DatabaseInfo appInfo : list) {
  //      res.add(new DatabaseInfoAdaptable(appInfo));
  //    }
  //    return res.toArray();
  //  }

}
