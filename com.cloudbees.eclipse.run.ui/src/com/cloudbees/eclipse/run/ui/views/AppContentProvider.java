package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.api.ApplicationListResponse;

final class AppContentProvider implements ITreeContentProvider {

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
      return new Object[] { "RUN@cloud apps" };
    }
    if (this.data != null && element instanceof String && ((String) element).startsWith("RUN@")) {
      return this.data.getApplications().toArray();
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
