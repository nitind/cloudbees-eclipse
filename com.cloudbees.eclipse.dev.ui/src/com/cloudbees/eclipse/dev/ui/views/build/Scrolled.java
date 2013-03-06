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
package com.cloudbees.eclipse.dev.ui.views.build;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class Scrolled {
  public static void main(String[] args) {
      Display display = new Display();
      Shell shell = new Shell(display);
      shell.setLayout(new GridLayout(2,false));
      //shell.setLayout(new FillLayout(SWT.HORIZONTAL));

      Composite content = new Composite(shell, SWT.BORDER);
    content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

      Composite sidebar = new Composite(shell, SWT.BORDER);
      sidebar.setLayout(new FillLayout(SWT.VERTICAL));

    sidebar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));

      Composite cc = new Composite(sidebar, SWT.BORDER);

      ScrolledComposite sc = new ScrolledComposite(sidebar, SWT.BORDER
                      | SWT.V_SCROLL | SWT.H_SCROLL);
      sc.setLayout(new GridLayout(1,true));

      Composite c = new Composite(sc, SWT.NONE);
      c.setSize(400, 400);
      c.setLayout(new GridLayout(1, true));

      for(int i = 0; i < 1000; i++){
      new Button(c, SWT.PUSH).setText("Text");
      }

      sc.setMinSize(c.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      sc.setContent(c);
      sc.setExpandHorizontal(true);
      sc.setExpandVertical(true);
    //sc.setAlwaysShowScrollBars(true);

      shell.open();
      while (!shell.isDisposed()) {
              if (!display.readAndDispatch())
                      display.sleep();
      }
      display.dispose();
  }
}