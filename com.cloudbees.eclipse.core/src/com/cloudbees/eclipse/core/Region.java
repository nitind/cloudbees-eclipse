package com.cloudbees.eclipse.core;

public enum Region {

  US("US", "https://api.cloudbees.com/api"),
  EU("EU", "https://api-eu.cloudbees.com/api");
  
  private final String label;
  private final String apiUrl;
  
  private Region(String label, String apiUrl) {
    this.label = label;
    this.apiUrl = apiUrl;
  }
  
  public String getLabel() {
    return label;
  }
  
  public String getApiUrl() {
    return apiUrl;
  }
  
}
