package com.cloudbees.eclipse.core.nectar.api;

import java.lang.reflect.Field;

public class NectarInstanceResponse {

  public String mode; // NORMAL
  public String nodeDescription;
  public String nodeName;
  public String description;
  public Boolean useSecurity;

  public PrimaryView primaryView;
  public View[] views;

  private final static String PACKAGE_NAME = NectarInstanceResponse.class.getPackage().getName();
  private final static String TREE_QUERY = buildTreeQuery(NectarInstanceResponse.class, new StringBuffer()).toString();

  public static class PrimaryView {
    public String name;
    public String url;
  }

  public static class View {
    public String name;
    public String url;
    //public Job[] jobs;
  }

  public static class Job {
    public HealthReport[] healthReport;

    public String displayName;
    public Boolean inQueue;
    public String color;

    public Boolean buildable;

    public Build lastBuild;
    public Build lastCompletedBuild;
    public Build lastFailedBuild;
    public Build lastStableBuild;
    public Build lastSuccessfulBuild;
    public Build lastUnstableBuild;
    public Build lastUnsuccessfulBuild;

    public static class Build {
      public String fullDisplayName;
      public String url;
      public String builtOn;
    }

    public static class HealthReport {

      public String description;
      public String iconUrl;
      public Long score;

    }

  }

  private final static StringBuffer buildTreeQuery(Class<?> baseClass, StringBuffer sb) {

    Field[] fields = baseClass.getFields();
    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];
      sb.append(field.getName());

      Class<?> cl = field.getType();

      if (cl.getPackage() != null && PACKAGE_NAME.equals(cl.getPackage().getName())) {
        sb.append("[");
        buildTreeQuery(cl, sb);
        sb.append("]");
      } else if (cl.isArray()) {
        sb.append("[");
        buildTreeQuery(cl.getComponentType(), sb);
        sb.append("]");
      }

      if (i < fields.length - 1) {
        sb.append(",");
      }

    }

    return sb;
  }

  public final static String getTreeQuery() {
    return TREE_QUERY;
  }  
  
  public static void main(String[] args) {
    System.out.println(NectarInstanceResponse.getTreeQuery());
  }
  
}
