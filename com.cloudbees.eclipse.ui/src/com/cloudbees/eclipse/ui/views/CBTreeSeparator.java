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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;

public class CBTreeSeparator extends Separator implements CBTreeContributor {

  public enum SeparatorLocation {
    POP_UP, PULL_DOWN, TOOL_BAR
  }

  private final SeparatorLocation location;

  public CBTreeSeparator(SeparatorLocation location) {
    this.location = location;
  }

  public void contributeTo(IMenuManager menuManager) {
    menuManager.add(this);
  }

  public void contributeTo(IToolBarManager toolBarManager) {
    toolBarManager.add(this);
  }

  public boolean isPopup() {
    return this.location == SeparatorLocation.POP_UP;
  }

  public boolean isPullDown() {
    return this.location == SeparatorLocation.PULL_DOWN;
  }

  public boolean isToolbar() {
    return this.location == SeparatorLocation.TOOL_BAR;
  }
}
