/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    this.forgeGroup.setLoading(true);
  }

  @Override
  public void inputChanged(final Viewer v, final Object oldInput, final Object newInput) {
    this.forgeGroup.clear();

    this.forgeGroup.setLoading(false);

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
