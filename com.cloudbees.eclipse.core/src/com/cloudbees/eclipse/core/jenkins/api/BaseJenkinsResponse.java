package com.cloudbees.eclipse.core.jenkins.api;

import com.google.gson.annotations.Expose;

abstract public class BaseJenkinsResponse {

  @Expose(deserialize = false, serialize = false)
  public String serviceUrl;

  /**
   * <code>null</code> if default view
   */
  @Expose(deserialize = false, serialize = false)
  public String viewUrl;

}
