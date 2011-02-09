package com.cloudbees.eclipse.core.internal.forge;

import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.repo.SVNRepositories;

import com.cloudbees.eclipse.core.CloudBeesException;

/**
 * Forge repo sync provider for Subclipse
 * 
 * @author ahtik
 */
public class ForgeSubclipseSync implements ForgeSync {

  public void sync(TYPE type, Properties props, IProgressMonitor monitor) throws CloudBeesException {

    if (!ForgeSync.TYPE.SVN.equals(type)) {
      return;
    }

    String url = props.getProperty("url");

    if (url == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    SVNRepositories repos = SVNProviderPlugin.getPlugin().getRepositories();
    boolean exists = repos.isKnownRepository(url, false);
    if (exists) {
      return;
    }

    try {
      monitor.beginTask("Validating SVN repository connection...", 10);
      monitor.worked(1);

      ISVNRepositoryLocation rep = SVNProviderPlugin.getPlugin().getRepositories().createRepository(props);
      monitor.worked(5);

      //rep.validateConnection(monitor);
      SVNProviderPlugin.getPlugin().getRepositories().addOrUpdateRepository(rep);
      monitor.worked(4);

    } catch (SVNException e) {
      throw new CloudBeesException("Failed to create missing repository!", e);
    } finally {
      monitor.worked(10);
      monitor.done();
    }
  }

}
