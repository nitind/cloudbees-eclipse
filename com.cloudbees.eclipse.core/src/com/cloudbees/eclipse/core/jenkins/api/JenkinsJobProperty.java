package com.cloudbees.eclipse.core.jenkins.api;

public class JenkinsJobProperty {

  public ParameterDefinition[] parameterDefinitions;

  public static class ParameterDefinition {
    public String name;
    public String description;
    public ParameterValue defaultParameterValue;
  }

  public static class ParameterValue {
    public String name;
    public String value;
  }
}
