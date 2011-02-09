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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((serviceUrl == null) ? 0 : serviceUrl.hashCode());
    result = prime * result + ((viewUrl == null) ? 0 : viewUrl.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BaseJenkinsResponse)) {
      return false;
    }
    BaseJenkinsResponse other = (BaseJenkinsResponse) obj;
    if (serviceUrl == null) {
      if (other.serviceUrl != null) {
        return false;
      }
    } else if (!serviceUrl.equals(other.serviceUrl)) {
      return false;
    }
    if (viewUrl == null) {
      if (other.viewUrl != null) {
        return false;
      }
    } else if (!viewUrl.equals(other.viewUrl)) {
      return false;
    }
    return true;
  }

}
