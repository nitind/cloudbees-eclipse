package com.cloudbees.eclipse.core;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Logger {

  private ILog log;

  public Logger(ILog log) {
    this.log = log;
  }

  public void log(int severity, String msg) {
    log.log(new Status(severity, log.getBundle().getSymbolicName(), msg));
  }

  public void info(String msg) {
    log(IStatus.INFO, msg);
  }

  public void warn(String msg) {
    log(IStatus.WARNING, msg);
  }

  public void error(String msg) {
    log(IStatus.ERROR, msg);
  }

}
