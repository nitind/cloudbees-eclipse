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
package com.cloudbees.eclipse.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class CBContentProvider implements IStructuredContentProvider, ITreeContentProvider {

  private CBTreeView owner;

  public CBContentProvider(final CBTreeView owner) {
    this.owner = owner;
  }

  public void dispose() {
    this.owner = null;
  }

  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    for (ICBTreeProvider provider : this.owner.getProviders()) {
      if (provider.getContentProvider()==null) {
        continue;
      }
      provider.getContentProvider().inputChanged(viewer, oldInput, newInput);
    }
  }

  public Object[] getChildren(final Object parentElement) {

    List<Object> children = new ArrayList<Object>();

    for (ICBTreeProvider provider : this.owner.getProviders()) {
      if (provider.getContentProvider()==null) {
        continue;
      }
      Object[] chlds = provider.getContentProvider().getChildren(parentElement);
      if (chlds != null) {
        children.addAll(Arrays.asList(chlds));
      }
    }

    return children.toArray(new Object[children.size()]);
  }

  public Object getParent(final Object element) {
    for (ICBTreeProvider provider : this.owner.getProviders()) {
      if (provider.getContentProvider()==null) {
        continue;
      }
      Object parent = provider.getContentProvider().getParent(element);
      if (parent != null) {
        return parent;
      }
    }

    return null;
  }

  public boolean hasChildren(final Object element) {
    for (ICBTreeProvider provider : this.owner.getProviders()) {
      if (provider.getContentProvider()==null) {
        continue;
      }
      if (provider.getContentProvider().hasChildren(element)) {
        return true;
      }
    }
    return false;
  }

  public Object[] getElements(final Object inputElement) {
    return getChildren(inputElement);
  }

}
