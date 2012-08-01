package com.cloudbees.eclipse.ui.views;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

public interface ICBTreeProvider {

  CBTreeContributor[] getContributors();

  ITreeContentProvider getContentProvider();

  ILabelProvider getLabelProvider();

  /**
   * @param object
   * @return true if this provider handled the request and no further handling is required
   */
  boolean open(Object object);

  void dispose();

  void setViewer(TreeViewer viewer);

  String getId();
  
  
}
