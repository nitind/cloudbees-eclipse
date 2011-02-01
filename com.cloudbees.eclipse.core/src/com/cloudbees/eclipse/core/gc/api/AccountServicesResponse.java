package com.cloudbees.eclipse.core.gc.api;

public class AccountServicesResponse {

  public String account_name;
  public JaasService[] jaas;
  public ForgeService[] forge;
  
  // For error reporting
  public String message;

  public static class ForgeService {
    public String url;
    public String type; // GIT, SVN
  }
  
  public static class JaasService {
    String instance_id;
  }

}
