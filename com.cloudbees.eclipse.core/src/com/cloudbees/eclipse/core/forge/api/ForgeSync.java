package com.cloudbees.eclipse.core.forge.api;

import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;

public interface ForgeSync {

  enum TYPE {
    SVN, GIT
  };
  
  enum ACTION {
    CHECKED("Checked"), ADDED("Added"), CLONED("Cloned"), SKIPPED("Skipped"), CANCELLED("Cancelled");
    
    private String label;
    
    private ACTION(String label) {
      this.label = label;
    }

    public String getLabel() {
      return this.label;
    }
  };

  ACTION sync(TYPE type, Properties props, IProgressMonitor monitor) throws CloudBeesException;

}
