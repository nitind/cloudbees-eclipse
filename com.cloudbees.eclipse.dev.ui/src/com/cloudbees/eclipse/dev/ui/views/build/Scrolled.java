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