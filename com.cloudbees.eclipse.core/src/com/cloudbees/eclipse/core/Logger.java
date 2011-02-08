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

  public void log(int severity, String msg, Throwable exception) {
    log.log(new Status(severity, log.getBundle().getSymbolicName(), msg, exception));
  }

  public void info(String msg) {
    log(IStatus.INFO, msg);
  }

  public void info(String msg, Throwable exception) {
    log(IStatus.INFO, msg, exception);
  }

  public void warn(String msg) {
    log(IStatus.WARNING, msg);
  }

  public void warn(String msg, Throwable exception) {
    log(IStatus.WARNING, msg, exception);
  }

  public void error(String msg) {
    log(IStatus.ERROR, msg);
  }

  public void error(String msg, Throwable exception) {
    log(IStatus.ERROR, msg, exception);
  }

}
