package com.cloudbees.eclipse.core.nectar.api;

import com.google.gson.annotations.Expose;

abstract public class BaseNectarResponse {

  @Expose(deserialize = false, serialize = false)
  public String serviceUrl;

  /**
   * <code>null</code> if default view
   */
  @Expose(deserialize = false, serialize = false)
  public String viewUrl;

}
