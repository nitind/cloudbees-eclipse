package com.cloudbees.eclipse.core.nectar.api;

import com.google.gson.annotations.Expose;



public class NectarInstanceResponse extends BaseNectarResponse {

  public String mode; // NORMAL
  public String nodeDescription;
  public String nodeName;
  public String description;
  public Boolean useSecurity;
  public boolean atCloud;

  public PrimaryView primaryView;
  public View[] views;

  @Expose(deserialize = false, serialize = false)
  public boolean offline = false;

  @Expose(deserialize = false, serialize = false)
  public String label;

  @Expose(deserialize = false, serialize = false)
  public final static String QTREE = QTreeFactory.create(NectarInstanceResponse.class);

  public NectarInstanceResponse() {

  }

  public static class PrimaryView {
    public String name;
    public String url;
  }

  public static class View {
    public String name;
    public String url;

    @Expose(deserialize = false, serialize = false)
    public boolean isPrimary;

    @Expose(deserialize = false, serialize = false)
    public NectarInstanceResponse response;

    //public Job[] jobs;
  }

}
