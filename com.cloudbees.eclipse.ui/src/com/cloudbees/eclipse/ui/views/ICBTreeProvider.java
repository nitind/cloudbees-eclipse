package com.cloudbees.eclipse.ui.views;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;

public interface ICBTreeProvider {

  CBTreeContributor[] getContributors();

  ITreeContentProvider getContentProvider();

  ILabelProvider getLabelProvider();

  boolean open(Object object);

  void dispose();

  void setViewer(TreeViewer viewer);

  String getId();
  
  
}
