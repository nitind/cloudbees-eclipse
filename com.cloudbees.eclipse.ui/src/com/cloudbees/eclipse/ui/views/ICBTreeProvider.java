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
