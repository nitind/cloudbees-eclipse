package com.cloudbees.eclipse.dev.ui.views.instances;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;

public class InstanceContentProvider implements IStructuredContentProvider, ITreeContentProvider {
  private InstanceGroup jenkinsGroup = new InstanceGroup("Jenkins", false);
  private InstanceGroup cloudGroup = new InstanceGroup("DEV@cloud", true);

  public InstanceContentProvider() {
    jenkinsGroup.setLoading(true);
    cloudGroup.setLoading(true);
  }

  public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    jenkinsGroup.clear();
    cloudGroup.clear();

    if (newInput == null || !(newInput instanceof List)) {
      v.refresh();
      return;
    }

    for (Object instance : (List) newInput) {
      if (instance instanceof JenkinsInstanceResponse) {
        JenkinsInstanceResponse nir = (JenkinsInstanceResponse) instance;
        if (nir.atCloud) {
          cloudGroup.addChild(nir);
        } else {
          jenkinsGroup.addChild(nir);
        }
      }
    }

    jenkinsGroup.setLoading(false);
    cloudGroup.setLoading(false);

    v.refresh();
    ((TreeViewer) v).expandToLevel(2);
  }

  public void dispose() {
    jenkinsGroup = null;
    cloudGroup = null;
  }

  public Object[] getElements(Object parent) {
    return getChildren(parent);
  }

  public Object getParent(Object child) {
    if (child instanceof JenkinsInstanceHolder) {
      return ((JenkinsInstanceHolder) child).getParent();
    }
    return null;
  }

  public Object[] getChildren(Object parent) {
    if (parent instanceof IViewSite) {
      return new InstanceGroup[] { cloudGroup, jenkinsGroup };
    } else
    if (parent instanceof InstanceGroup) {
      return ((InstanceGroup) parent).getChildren();
    } else
    if (parent instanceof JenkinsInstanceResponse) {
      JenkinsInstanceResponse resp = (JenkinsInstanceResponse) parent;
      return resp.views;
    }
    return new Object[0];
  }

  public boolean hasChildren(Object parent) {
    if (parent instanceof InstanceGroup)
      return ((InstanceGroup) parent).hasChildren();

    if (parent instanceof JenkinsInstanceResponse) {
      return ((JenkinsInstanceResponse) parent).views != null && ((JenkinsInstanceResponse) parent).views.length > 0;
    }

    return false;
  }
}
