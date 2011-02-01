package com.cloudbees.eclipse.core.nectar.api;

/**
 * Main response object for the job view
 * 
 * @author ahti
 */
public class NectarJobInfoResponse {

  //Build info!
  class Build {
    public Boolean building;
    public String builtOn;
    public Long duration;
    public String id;
    public Long number;
    public Long timestamp;
    public String url;
    public String fullDisplayName;

  }

}
