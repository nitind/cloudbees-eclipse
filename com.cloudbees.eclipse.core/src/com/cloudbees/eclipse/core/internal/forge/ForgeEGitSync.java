package com.cloudbees.eclipse.core.internal.forge;

import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;

import com.cloudbees.eclipse.core.CloudBeesException;

/**
 * Forge repo sync provider for EGIT
 * 
 * @author ahtik
 */
public class ForgeEGitSync implements ForgeSync {

  public void sync(TYPE type, Properties props, IProgressMonitor monitor) throws CloudBeesException {

    if (!ForgeSync.TYPE.GIT.equals(type)) {
      return;
    }

    String url = props.getProperty("url");

    if (url == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    //File repoFile = new File(url);

    //TODO Implement Clone operation after more git info is available from the Forge repo meta data
    //CloneOperation clone = new CloneOperation(uri, allSelected, selectedBranches, workdir, branch, remoteName, timeout);

  }

}
