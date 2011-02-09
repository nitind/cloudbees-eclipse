package com.cloudbees.eclipse.core.internal.forge;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.jgit.transport.URIish;

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

    monitor.setTaskName("Creating EGit repository:");
    monitor.worked(2);
    //TODO Implement Clone operation after more git info is available from the Forge repo meta data
    try {
      CloneOperation clone = new CloneOperation(new URIish(url), true, Collections.EMPTY_LIST, new File(""), null,
          "origin", 5000);
      monitor.worked(5);
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      monitor.done();
    }
  }

}
