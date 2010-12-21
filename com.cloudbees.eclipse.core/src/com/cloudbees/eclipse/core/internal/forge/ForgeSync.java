package com.cloudbees.eclipse.core.internal.forge;

import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;

public interface ForgeSync {

  enum TYPE {
    SVN, GIT
  };

  void sync(TYPE type, Properties props, IProgressMonitor monitor) throws CloudBeesException;

}
