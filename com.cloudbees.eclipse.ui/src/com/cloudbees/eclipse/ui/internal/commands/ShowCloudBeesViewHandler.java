package com.cloudbees.eclipse.ui.internal.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ShowCloudBeesViewHandler extends AbstractHandler {

  public Object execute(ExecutionEvent event) throws ExecutionException {
    try {
      PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
          .showView("com.cloudbees.eclipse.ui.views.CBTreeView");
    } catch (PartInitException e) {
      e.printStackTrace();
    }
    return null;
  }

}
