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
package com.cloudbees.eclipse.ui;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class UIUtils {

  public static final Point getCenterPoint() {
    Shell main = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    Rectangle rect = main.getBounds();
    return new Point(rect.x + rect.width / 2, (rect.y + rect.height) / 2);
  }

}
