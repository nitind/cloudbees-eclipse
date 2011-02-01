package com.cloudbees.eclipse.core.domain;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.cloudbees.eclipse.util.Utils;
import com.google.gson.reflect.TypeToken;

public class NectarInstance {

  public String label;
  public String url;
  public String username;
  public String password;
  public boolean authenticate;

  public NectarInstance() {
    
  }
  
  public NectarInstance(String label, String url, String username, String password, boolean authenticate) {
    this.label = label;
    this.url = url;
    this.username = username;
    this.password = password;
    this.authenticate = authenticate;
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