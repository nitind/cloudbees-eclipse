package com.cloudbees.eclipse.run.ui.console;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class TerminateHandler extends AbstractHandler {

  public Object execute(ExecutionEvent event) throws ExecutionException {
    System.out.println("Execute terminate");
    
    return null;
  }

}
