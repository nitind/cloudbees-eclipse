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
package com.cloudbees.eclipse.dev.ui.views.forge;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;

public class ForgeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
  private ForgeGroup forgeGroup = new ForgeGroup("Repositories");

  public ForgeContentProvider() {
    this.forgeGroup.setLoading(false);
  }

  @Override
  public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {

    this.forgeGroup.setLoading(false);

    if (newInput == null || !(newInput instanceof List)) {
      if (v != null) {
        v.refresh();
      }
      return;
    }

    this.forgeGroup.clear();

    for (Object instance : (List) newInput) {
      if (instance instanceof ForgeInstance) {
        this.forgeGroup.addChild((ForgeInstance) instance);
      }
    }

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
