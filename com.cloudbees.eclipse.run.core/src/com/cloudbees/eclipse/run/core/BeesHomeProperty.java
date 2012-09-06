package com.cloudbees.eclipse.run.core;

import org.eclipse.ant.core.IAntPropertyValueProvider;

import com.cloudbees.eclipse.run.sdk.CBSdkActivator;

public class BeesHomeProperty implements IAntPropertyValueProvider {

  public String getAntPropertyValue(String antPropertyName) {
    return CBSdkActivator.getDefault().getBeesHome();
  }

}
