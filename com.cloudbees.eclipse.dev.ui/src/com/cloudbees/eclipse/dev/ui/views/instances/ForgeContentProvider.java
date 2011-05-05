package com.cloudbees.eclipse.dev.ui.views.instances;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;

public class ForgeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
  private ForgeGroup forgeGroup = new ForgeGroup("Forge repositories");

  public ForgeContentProvider() {
    this.forgeGroup.setLoading(true);
  }

  @Override
  public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
    this.forgeGroup.clear();

    if (newInput == null || !(newInput instanceof List)) {
      if (v != null) {
        v.refresh();
      }
      return;
    }

    for (Object instance : (List) newInput) {
      if (instance instanceof ForgeInstance) {
        this.forgeGroup.addChild((ForgeInstance) instance);
      }
    }

    this.forgeGroup.setLoading(false);

    if (v != null) {
      v.refresh();
      ((TreeViewer) v).expandToLevel(1);
    }
  }

  @Override
  public void dispose() {
    this.forgeGroup = null;
  }

  @Override
  public Object[] getElements(final Object parent) {
    return getChildren(parent);
  }

  @Override
  public Object getParent(final Object child) {
    return null;
  }

  @Override
  public Object[] getChildren(final Object parent) {
    if (parent instanceof IViewSite) {
      return new ForgeGroup[] { this.forgeGroup };
    } else if (parent instanceof ForgeGroup) {
      return ((ForgeGroup) parent).getChildren();
    }
    return new Object[0];
  }

  @Override
  public boolean hasChildren(final Object parent) {
    if (parent instanceof IViewSite) {
      return true;
    }
    if (parent instanceof ForgeGroup) {
      return ((ForgeGroup) parent).hasChildren();
    }

    return false;
  }
}
