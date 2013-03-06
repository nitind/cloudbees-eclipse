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
package com.cloudbees.eclipse.ui.internal;

import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.ui.CBImages;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.console.BeesConsole;
import com.cloudbees.eclipse.ui.console.BeesConsoleFactory;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ShowConsoleAction extends CBTreeAction {

  public ShowConsoleAction() {
    super(false);
    setText("CloudBees SDK Console");
    setToolTipText("CloudBees SDK Console");
    setImageDescriptor(CloudBeesUIPlugin.getImageDescription(CBImages.ICON_16X16_CB_CONSOLE));     
  }

  @Override
  public void run() {
    PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

      public void run() {
        BeesConsoleFactory.openGlobalConsole();
        BeesConsole.moveCaret();
        BeesConsole.focusConsole();
    }});

  }

  public boolean isPopup() {
    return false;
  }

  public boolean isPullDown() {
    return true;
  }

  public boolean isToolbar() {
    return false;
  }
}
