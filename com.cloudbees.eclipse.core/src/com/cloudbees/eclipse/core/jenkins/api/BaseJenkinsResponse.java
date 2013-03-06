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
package com.cloudbees.eclipse.core.jenkins.api;

import com.google.gson.annotations.Expose;

abstract public class BaseJenkinsResponse {

  /**
   * <code>null</code> if default view
   */
  @Expose(deserialize = false, serialize = false)
  public String viewUrl;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
