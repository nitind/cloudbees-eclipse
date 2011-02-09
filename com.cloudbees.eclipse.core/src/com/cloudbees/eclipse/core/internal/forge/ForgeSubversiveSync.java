package com.cloudbees.eclipse.core.internal.forge;

import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;

import com.cloudbees.eclipse.core.CloudBeesException;

public class ForgeSubversiveSync implements ForgeSync {

  public void sync(TYPE type, Properties props, IProgressMonitor monitor) throws CloudBeesException {

    if (!ForgeSync.TYPE.SVN.equals(type)) {
      return;
    }

    String url = props.getProperty("url");

    if (url == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    try {
      monitor.beginTask("Validating SVN repository connection...", 10);

      monitor.worked(1);
      IRepositoryLocation loc = SVNRemoteStorage.instance().newRepositoryLocation();

      monitor.worked(1);

      Exception ex = SVNUtility.validateRepositoryLocation(loc);
      if (ex != null) {
        throw new CloudBeesException("Failed to validate SVN connection to " + url, ex);
      }

      monitor.worked(1);

      monitor.setTaskName("Adding repository...");
      SVNRemoteStorage.instance().addRepositoryLocation(loc);
      monitor.worked(7);
    } finally {
      monitor.worked(10);
      monitor.done();
    }
  }

}
