package com.cloudbees.eclipse.core.nectar.api;



public class NectarInstanceResponse extends BaseNectarResponse {

  public String mode; // NORMAL
  public String nodeDescription;
  public String nodeName;
  public String description;
  public Boolean useSecurity;

  public PrimaryView primaryView;
  public View[] views;

  static {
    initTreeQuery(NectarInstanceResponse.class);
  }

  public NectarInstanceResponse() {

  }

  public static class PrimaryView {
    public String name;
    public String url;
  }

  public static class View {
    public String name;
    public String url;
    //public Job[] jobs;
  }


  public final static String getTreeQuery() {
    //enforce this subclass to be loaded
    return _getTreeQuery();
  }

}
