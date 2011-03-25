package com.cloudbees.eclipse.run.ui.contributions.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import com.cloudbees.eclipse.run.ui.wizards.CBSampleWebAppWizard;

public class CBSampleWebAppHandler extends  AbstractHandler {

  public Object execute(ExecutionEvent event) throws ExecutionException {

    IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
    WizardDialog dialog = new WizardDialog(window.getShell(), new CBSampleWebAppWizard());
    dialog.create();
    dialog.open();
    
    return null;
  }

}