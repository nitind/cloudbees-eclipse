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

    IRepositoryLocation loc = SVNRemoteStorage.instance().newRepositoryLocation();
    monitor.setTaskName("Validating SVN repository connection.");

    Exception ex = SVNUtility.validateRepositoryLocation(loc);
    if (ex != null) {
      throw new CloudBeesException("Failed to validate SVN connection to " + url, ex);
    }

    monitor.setTaskName("Adding repository.");
    SVNRemoteStorage.instance().addRepositoryLocation(loc);
    monitor.done();
  }

}
