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
