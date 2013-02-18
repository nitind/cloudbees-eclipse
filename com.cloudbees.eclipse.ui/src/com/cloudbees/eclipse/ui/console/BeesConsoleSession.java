package com.cloudbees.eclipse.ui.console;

import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

abstract public class BeesConsoleSession {

  abstract IOConsoleInputStream getInputStream();
  abstract IOConsoleOutputStream newOutputStream();
  
}
