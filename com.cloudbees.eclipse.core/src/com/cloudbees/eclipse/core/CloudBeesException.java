package com.cloudbees.eclipse.core;

public class CloudBeesException extends Exception {

  private static final long serialVersionUID = -6129647305172827815L;

  public CloudBeesException(String msg, Throwable t) {
    super(msg, t);
  }

  public CloudBeesException(Throwable t) {
    super(t);
  }

  public CloudBeesException(String msg) {
    super(msg);
  }

}
