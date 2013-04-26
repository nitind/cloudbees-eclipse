/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.core.domain;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.cloudbees.eclipse.core.util.Utils;
import com.google.gson.reflect.TypeToken;

public class JenkinsInstance implements Comparable<JenkinsInstance> {

  public String id = UUID.randomUUID().toString();

  public String label;
  public String url;
  public String username;
  public String password;
  public boolean authenticate;
  public boolean atCloud;
  
  public String alternativeUrl; // url provided by the primary view url. Initialized lazily whenever views get loaded.

  public JenkinsInstance() {
  }

  public JenkinsInstance(final String label, final String url) {
    this(label, url, null, null, false, false);
  }

  public JenkinsInstance(final String label, final String url, final String username, final String password, final boolean authenticate,
      final boolean atCloud) {
    this.label = label;
    this.url = url;
    this.username = username;
    this.password = password;
    this.authenticate = authenticate;
    this.atCloud = atCloud;
    //    this.id = url;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    JenkinsInstance other = (JenkinsInstance) obj;
    if (this.id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!this.id.equals(other.id)) {
      return false;
    }
    return true;
  }

  /** i.e. order by id for collections */
  public int compareTo(final JenkinsInstance other) {
    if (other == null) {
      return +1;
    }

    return this.id.compareTo(other.id);
  }

  /**
   * <p>
   * Encodes a list of JenkinsInstances to one base64 USASCII String. Internally it's kept in json format.
   * </p>
   * <p>
   * Possible use cases include storing this information in preferences.
   * </p>
   *
   * @param instances
   * @return
   */
  public final static String encode(final List<JenkinsInstance> instances) {
    Type type = new TypeToken<List<JenkinsInstance>>(){}.getType();
    String json = Utils.createGson().toJson(instances,type);
    return Utils.toB64(json);
  }

  /**
   * <p>
   * Decodes a string to an array of JenkinsInstances from base64 string.
   * </p>
   * <p>
   * Possible use cases include storing this information in preferences.
   * </p>
   *
   * @param encodedInstances
   * @return
   */
  public final static List<JenkinsInstance> decode(final String encodedInstances) {
    Type type = new TypeToken<List<JenkinsInstance>>(){}.getType();
    String json = Utils.fromB64(encodedInstances);
    List<JenkinsInstance> ret = Utils.createGson().fromJson(json, type);
    if (ret == null) {
      return new ArrayList<JenkinsInstance>();
    }
    return ret;
  }

  @Override
  public String toString() {
    StringBuffer ret = new StringBuffer();
    ret.append("JenkinsInstance[");
    ret.append("label=" + this.label + ";");
    ret.append("url=" + this.url + ";");
    ret.append("auth=" + this.authenticate + ";");
    ret.append("username=" + this.username + ";");
    ret.append("password=" + ((this.password != null && this.password.length() > 0) ? "******" : ""));
    ret.append("]");
    return ret.toString();
  }
}
