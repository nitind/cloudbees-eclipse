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
package com.cloudbees.eclipse.ui.console;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.IConsoleManager;

public class BeesConsoleFactory implements IConsoleFactory {

  private static IConsoleManager consoleManager;

  private static BeesConsole console;


  public void openConsole() {
    openGlobalConsole();
  }

  public static void openGlobalConsole() {
    if (consoleManager == null) {
      consoleManager = ConsolePlugin.getDefault().getConsoleManager();

      consoleManager.addConsoleListener(new IConsoleListener() {
        public void consolesAdded(IConsole[] consoles) {
        }

        public void consolesRemoved(IConsole[] consoles) {
          for (int i = 0; i < consoles.length; i++) {
            if (consoles[i] == console) {
              console = null;
            }
          }
        }

      });
    }
    
    if (console == null) {
      console = new BeesConsole();
      consoleManager.addConsoles(new IConsole[] { console });
    }
    consoleManager.showConsoleView(console);
  }

}
