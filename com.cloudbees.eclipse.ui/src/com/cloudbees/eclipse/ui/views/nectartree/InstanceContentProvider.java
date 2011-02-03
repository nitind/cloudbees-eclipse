package com.cloudbees.eclipse.ui.views.nectartree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class InstanceContentProvider implements IStructuredContentProvider, ITreeContentProvider {
  private List<InstanceGroup> root;
  private IViewSite site;

  public InstanceContentProvider(IViewSite iViewSite) {
    site = iViewSite;
  }

  public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    root = null;
  }

  public void dispose() {
  }

  public Object[] getElements(Object parent) {
    if (parent.equals(site)) {
      if (root == null) {
        try {
          initialize(null);
        } catch (Exception e) {
          e.printStackTrace();
          return new InstanceGroup[0];//service not available
        }
      }
      return root.toArray(new InstanceGroup[0]);
    }
    return getChildren(parent);
  }

  public Object getParent(Object child) {
    if (child instanceof NectarInstanceHolder) {
      return ((NectarInstanceHolder) child).getParent();
    }
    return null;
  }

  public Object[] getChildren(Object parent) {
    if (parent instanceof InstanceGroup) {
      return ((InstanceGroup) parent).getChildren();
    }
    if (parent instanceof NectarInstanceResponse) {
      NectarInstanceResponse resp = (NectarInstanceResponse) parent;
      return resp.views;
    }
    return new Object[0];
  }

  public boolean hasChildren(Object parent) {
    if (parent instanceof InstanceGroup)
      return ((InstanceGroup) parent).hasChildren();

    if (parent instanceof NectarInstanceResponse) {
      return ((NectarInstanceResponse) parent).views != null && ((NectarInstanceResponse) parent).views.length > 0;
    }

    return false;
  }

  private void initialize(IProgressMonitor monitor) throws CloudBeesException {
    //TreeObject to1 = new TreeObject("job 1");
    InstanceGroup p1 = new InstanceGroup("Nectar", false);

    List<NectarInstanceResponse> services = CloudBeesUIPlugin.getDefault().getManualNectarsInfo();
    Iterator<NectarInstanceResponse> it = services.iterator();
    while (it.hasNext()) {
      NectarInstanceResponse resp = (NectarInstanceResponse) it.next();
      p1.addChild(resp);
    }

    InstanceGroup p2 = new InstanceGroup("DEV@cloud", true);

    try {
      List<NectarInstanceResponse> dev = CloudBeesUIPlugin.getDefault().getDevAtCloudNectarsInfo(monitor);
      Iterator<NectarInstanceResponse> devit = dev.iterator();
      while (devit.hasNext()) {
        NectarInstanceResponse resp = (NectarInstanceResponse) devit.next();
        p2.addChild(resp);
      }
    } catch (CloudBeesException e) {
      // Let's ignore these errors so the tree for manual nectars remains available
      //TODO consider logging anyway
    }

    root = new ArrayList<InstanceGroup>();
    root.add(p1);
    root.add(p2);
  }
}
