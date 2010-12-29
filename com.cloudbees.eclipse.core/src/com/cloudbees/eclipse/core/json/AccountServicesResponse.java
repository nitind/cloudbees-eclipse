package com.cloudbees.eclipse.core.json;

public class AccountServicesResponse {

  public String account_name;
  public HaasService[] haas;
  public ForgeService[] forge;
  
  // For error reporting
  public String message;

}
