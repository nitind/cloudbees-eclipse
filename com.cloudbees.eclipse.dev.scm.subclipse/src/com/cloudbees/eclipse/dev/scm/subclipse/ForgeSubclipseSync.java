package com.cloudbees.eclipse.dev.scm.subclipse;

import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.SVNException;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.repo.SVNRepositories;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.forge.api.ForgeSync;

/**
 * Forge repo sync provider for Subclipse
 * 
 * @author ahtik
 */
public class ForgeSubclipseSync implements ForgeSync {

  public ACTION sync(TYPE type, Properties props, IProgressMonitor monitor) throws CloudBeesException {

    if (!ForgeSync.TYPE.SVN.equals(type)) {
      return ACTION.SKIPPED;
    }

    String url = props.getProperty("url");

    if (url == null) {
      throw new IllegalArgumentException("url not provided!");
    }

    try {
      monitor.beginTask("Validating SVN repository connection '" + url + "'...", 10);
      monitor.worked(1);

      SVNRepositories repos = SVNProviderPlugin.getPlugin().getRepositories();
      boolean exists = repos.isKnownRepository(url, false);
      if (exists) {
        monitor.worked(9);
        return ACTION.CHECKED;
      }

      ISVNRepositoryLocation rep = SVNProviderPlugin.getPlugin().getRepositories().createRepository(props);
      monitor.worked(5);

      //rep.validateConnection(monitor);
      SVNProviderPlugin.getPlugin().getRepositories().addOrUpdateRepository(rep);
      monitor.worked(4);

      return ACTION.ADDED;

    } catch (SVNException e) {
      throw new CloudBeesException("Failed to create missing repository!", e);
    } finally {
      monitor.worked(10);
      monitor.done();
    }
  }

}
