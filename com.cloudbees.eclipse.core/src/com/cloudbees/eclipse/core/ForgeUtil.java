package com.cloudbees.eclipse.core;

public class ForgeUtil {
  
  public final static String PR_SSH = "ssh://git@";
  public final static String PR_HTTPS = "https://";

  public final static String stripGitPrefixes(String url) {
    
    if (url!=null && url.toLowerCase().startsWith(PR_SSH)) {
      return url.substring(PR_SSH.length());
    }
    
    if (url!=null && url.toLowerCase().startsWith(PR_HTTPS)) {
      return url.substring(PR_HTTPS.length());
    }
    
    return url;
  
  }

}
