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
      provider.getContentProvider().inputChanged(viewer, oldInput, newInput);
    }
  }

  public Object[] getChildren(final Object parentElement) {

    List<Object> children = new ArrayList<Object>();

    for (ICBTreeProvider provider : this.owner.getProviders()) {
      Object[] chlds = provider.getContentProvider().getChildren(parentElement);
      if (chlds != null) {
        children.addAll(Arrays.asList(chlds));
      }
    }

    return children.toArray(new Object[children.size()]);
  }

  public Object getParent(final Object element) {
    for (ICBTreeProvider provider : this.owner.getProviders()) {
      Object parent = provider.getContentProvider().getParent(element);
      if (parent != null) {
        return parent;
      }
    }

    return null;
  }

  public boolean hasChildren(final Object element) {
    for (ICBTreeProvider provider : this.owner.getProviders()) {
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
