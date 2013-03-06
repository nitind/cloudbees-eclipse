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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;

import com.cloudbees.eclipse.ui.AuthStatus;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public abstract class CBTreeAction extends Action implements CBTreeContributor {

  private boolean authrequired;
  
  public CBTreeAction(boolean authrequired) {
    super();
    this.authrequired=authrequired;
  }
  
  public void contributeTo(IMenuManager menuManager) {
    menuManager.add(this);
  }

  public void contributeTo(IToolBarManager toolBarManager) {
    toolBarManager.add(this);
  }
  /*
  @Override
  public boolean isEnabled() {
    if (!super.isEnabled()) {
      return false;
    }
    
  }*/

  final public void selectionChanged(IAction action, ISelection selection) {
      action.setEnabled((!authrequired || CloudBeesUIPlugin.getDefault().getAuthStatus()==AuthStatus.OK ) && super.isEnabled());
  }

  final public boolean isEnabled() {
    return (!authrequired || CloudBeesUIPlugin.getDefault().getAuthStatus()==AuthStatus.OK) && super.isEnabled();
  }
  
}
