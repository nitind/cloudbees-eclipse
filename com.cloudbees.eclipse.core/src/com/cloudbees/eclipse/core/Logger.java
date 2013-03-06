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

  public void warn(Throwable exception) {
    log(IStatus.WARNING, exception.getLocalizedMessage(), exception);
  }

  public void error(String msg) {
    log(IStatus.ERROR, msg);
  }

  public void error(String msg, Throwable exception) {
    log(IStatus.ERROR, msg, exception);
  }

  public void error(Throwable exception) {
    log(IStatus.ERROR, exception.getLocalizedMessage(), exception);
  }

}
