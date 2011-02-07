package com.cloudbees.eclipse.ui.views.nectartree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;

public class InstanceContentProvider implements IStructuredContentProvider, ITreeContentProvider {
  private List<InstanceGroup> root;

  public InstanceContentProvider() {
  }

  public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    if (newInput == null || !(newInput instanceof List)) {
      root = null; // gone
      v.refresh();
      return;
    }

    //TreeObject to1 = new TreeObject("job 1");
    InstanceGroup jenkinsGroup = new InstanceGroup("Jenkins", false);
    InstanceGroup cloudGroup = new InstanceGroup("DEV@cloud", true);

    root = new ArrayList<InstanceGroup>();
    root.add(jenkinsGroup);
    root.add(cloudGroup);

    for (Object instance : (List) newInput) {
      if (instance instanceof NectarInstanceResponse) {
        NectarInstanceResponse nir = (NectarInstanceResponse) instance;
        if (nir.atCloud) {
          cloudGroup.addChild(nir);
        } else {
          jenkinsGroup.addChild(nir);
        }
      }
    }

    v.refresh();
  }

  public void dispose() {
    root = null;
  }

  public Object[] getElements(Object parent) {
    return getChildren(parent);
  }

  public Object getParent(Object child) {
    if (child instanceof NectarInstanceHolder) {
      return ((NectarInstanceHolder) child).getParent();
    }
    return null;
  }

  public Object[] getChildren(Object parent) {
    if (parent instanceof IViewSite && root != null) {
      return root.toArray(new InstanceGroup[root.size()]);
    } else
    if (parent instanceof InstanceGroup) {
      return ((InstanceGroup) parent).getChildren();
    } else
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
}
