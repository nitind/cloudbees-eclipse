package com.cloudbees.eclipse.dev.ui.views.instances;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;

public class InstanceContentProvider implements IStructuredContentProvider, ITreeContentProvider {
  private InstanceGroup jenkinsGroup = new InstanceGroup.OnPremiseJenkinsInstanceGroup("On-premise Jenkins", false);
  private InstanceGroup cloudGroup = new InstanceGroup.DevAtCloudJenkinsInstanceGroup("DEV@cloud Jenkins", true);
  private InstanceGroup favoritesGroup = new FavoritesInstanceGroup("Favorite Jenkins Jobs", false);

  public InstanceContentProvider() {
    this.jenkinsGroup.setLoading(true);
    this.cloudGroup.setLoading(true);
    this.favoritesGroup.setLoading(false);
  }

  @Override
  public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
    this.jenkinsGroup.clear();
    this.cloudGroup.clear();

    if (newInput == null || !(newInput instanceof List)) {
      if (v != null) {
        v.refresh();
      }
      return;
    }

    for (Object instance : (List) newInput) {
      if (instance instanceof JenkinsInstanceResponse) {
        JenkinsInstanceResponse nir = (JenkinsInstanceResponse) instance;
        if (nir.atCloud) {
          this.cloudGroup.addChild(nir);
        } else {
          this.jenkinsGroup.addChild(nir);
        }
      }
    }

    this.jenkinsGroup.setLoading(false);
    this.cloudGroup.setLoading(false);

    if (v != null) {
      v.refresh();
      ((TreeViewer) v).expandToLevel(1);
    }
  }

  @Override
  public void dispose() {
    this.jenkinsGroup = null;
    this.cloudGroup = null;
    this.favoritesGroup = null;
  }

  @Override
  public Object[] getElements(final Object parent) {
    return getChildren(parent);
  }

  @Override
  public Object getParent(final Object child) {
    if (child instanceof JenkinsInstanceHolder) {
      return ((JenkinsInstanceHolder) child).getParent();
    }
    return null;
  }

  @Override
  public Object[] getChildren(final Object parent) {
    if (parent instanceof IViewSite) {
      return new InstanceGroup[] { this.cloudGroup, this.jenkinsGroup, this.favoritesGroup };
    } else if (parent instanceof InstanceGroup) {
      return ((InstanceGroup) parent).getChildren();
    } else if (parent instanceof JenkinsInstanceResponse) {
      JenkinsInstanceResponse resp = (JenkinsInstanceResponse) parent;
      return resp.views;
    }
    return new Object[0];
  }

  @Override
  public boolean hasChildren(final Object parent) {
    if (parent instanceof InstanceGroup) {
      return ((InstanceGroup) parent).hasChildren();
    }

    if (parent instanceof JenkinsInstanceResponse) {
      return ((JenkinsInstanceResponse) parent).views != null && ((JenkinsInstanceResponse) parent).views.length > 0;
    }

    return false;
  }
}
