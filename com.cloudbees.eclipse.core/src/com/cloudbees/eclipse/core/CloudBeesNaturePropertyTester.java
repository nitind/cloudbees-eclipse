package com.cloudbees.eclipse.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;

public class CloudBeesNaturePropertyTester extends PropertyTester {
  
  public static final String IS_CLOUDBEES_NATURE = "isCloudBeesNature";
  public static final String IS_NOT_CLOUDBEES_NATURE = "isNotCloudBeesNature";
  
  public CloudBeesNaturePropertyTester() {
  }

  public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
    if(!(receiver instanceof IProject)) {
      return false;
    }
    
    IProject project = (IProject) receiver;
    
    if(property.equals(IS_CLOUDBEES_NATURE)) {
      return NatureUtil.isEnabledFor(project, CloudBeesNature.NATURE_ID);
    } else if (property.equals(IS_NOT_CLOUDBEES_NATURE)) {
      return !NatureUtil.isEnabledFor(project, CloudBeesNature.NATURE_ID);
    }
    
    return false;
  }

}