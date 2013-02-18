package com.cloudbees.eclipse.core.gc.api;


import com.google.gson.annotations.SerializedName;

public class ClickStartTemplate extends ClickStartResponseBase {
  
  public String id;
  public String name;
  public String icon;
  public String description;
  public Component[] components;
  
  @SerializedName("doc-url") public String docUrl;

  public static class Component {
    public String name;
    public String icon;
    public String description;
    public String url;
    public String managementUrl;
    public String key;
  }
  
  public String toString() {
    return name;
  }
}
