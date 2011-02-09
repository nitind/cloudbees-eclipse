package com.cloudbees.eclipse.core.internal.forge;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.jgit.transport.URIish;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.util.Utils;

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

    try {
      monitor.beginTask("Creating EGit repository...", 10);
      monitor.worked(2);

      CloneOperation clone = Utils.createInstance(CloneOperation.class, new Class[] { URIish.class, Boolean.TYPE,
          Collection.class, File.class, String.class, String.class, Integer.TYPE }, new Object[] { new URIish(url),
          true, Collections.EMPTY_LIST, new File(""), null, "origin", 5000 });
      if (clone == null) {
        // old constructor didn't have timeout at the end
        clone = Utils.createInstance(CloneOperation.class, new Class[] { URIish.class, Boolean.TYPE, Collection.class,
            File.class, String.class, String.class }, new Object[] { new URIish(url), true, Collections.EMPTY_LIST,
            new File(""), null, "origin" });
      }

      monitor.worked(2);

      clone.run(monitor);

      if (clone == null) {
        throw new CloudBeesException("Failed to create EGit clone operation");
      }

      monitor.worked(6);

    } catch (URISyntaxException e) {
      throw new CloudBeesException(e);
    } catch (InvocationTargetException e) {
      throw new CloudBeesException(e);
    } catch (InterruptedException e) {
      throw new CloudBeesException(e);
    } finally {
      monitor.worked(10);
      monitor.done();
    }
  }

}
