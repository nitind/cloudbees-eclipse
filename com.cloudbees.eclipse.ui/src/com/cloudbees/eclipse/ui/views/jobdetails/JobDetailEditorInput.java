package com.cloudbees.eclipse.ui.views.jobdetails;

import org.eclipse.ui.internal.part.NullEditorInput;

public class JobDetailEditorInput extends NullEditorInput {

  public JobDetailEditorInput(String jobUrl) {
    System.out.println("Creating job details editor for url " + jobUrl);
  }

}
