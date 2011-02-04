package com.cloudbees.eclipse.core.domain;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.cloudbees.eclipse.core.util.Utils;
import com.google.gson.reflect.TypeToken;

public class NectarInstance implements Comparable<NectarInstance> {

  public String id = UUID.randomUUID().toString();

  public String label;
  public String url;
  public String username;
  public String password;
  public boolean authenticate;
  public boolean atCloud;

  public NectarInstance() {
  }
  
  public NectarInstance(String label, String url) {
    this(label, url, null, null, false, false);
  }

  public NectarInstance(String label, String url, String username, String password, boolean authenticate,
      boolean atCloud) {
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
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    NectarInstance other = (NectarInstance) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

  /** i.e. order by id for collections */
  public int compareTo(NectarInstance other) {
    if (other == null) {
      return +1;
    }

    return this.id.compareTo(other.id);
  }

  /**
   * <p>
   * Encodes a list of NectarInstances to one base64 USASCII String. Internally it's kept in json format.
   * </p>
   * <p>
   * Possible use cases include storing this information in preferences.
   * </p>
   * 
   * @param instances
   * @return
   */
  public final static String encode(List<NectarInstance> instances) {
    Type type = new TypeToken<List<NectarInstance>>(){}.getType();
    return Utils.toB64(Utils.createGson().toJson(instances,type));
  }

  /**
   * <p>
   * Decodes a string to an array of NectarInstances from base64 string.
   * </p>
   * <p>
   * Possible use cases include storing this information in preferences.
   * </p>
   * 
   * @param encodedInstances
   * @return
   */
  @SuppressWarnings("unchecked")
  public final static List<NectarInstance> decode(String encodedInstances) {
    Type type = new TypeToken<List<NectarInstance>>(){}.getType();
    List<NectarInstance> ret = Utils.createGson().fromJson(Utils.fromB64(encodedInstances), type);
    if (ret == null) {
      return new ArrayList<NectarInstance>();
    }
    return ret;
  }

  @Override
  public String toString() {
    StringBuffer ret = new StringBuffer();
    ret.append("NectarInstance[");
    ret.append("label=" + label + ";");
    ret.append("url=" + url + ";");
    ret.append("username=" + username + ";");
    ret.append("password=" + ((password != null && password.length() > 0) ? "******" : ""));
    ret.append("]");
    return ret.toString();
  }
}
