/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
package com.cloudbees.eclipse.dev.ui.views.build;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;

import com.cloudbees.eclipse.core.jenkins.api.ArtifactPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Artifact;

public class ArtifactsClickListener implements IDoubleClickListener {

  @Override
  public void doubleClick(final DoubleClickEvent event) {
    Object selection = ((TreeSelection) event.getSelection()).getFirstElement();

    if (!(selection instanceof ArtifactPathItem)) {
      return;
    }

    final ArtifactPathItem item = (ArtifactPathItem) selection;
    final JenkinsBuildDetailsResponse build = item.parent;
    final Artifact artifact = item.item;

    if (!artifact.relativePath.endsWith(".war")) {
      return;
    }

  }

  protected static String getWarUrl(final JenkinsBuildDetailsResponse build, final Artifact artifact) {
    String warPath = artifact.relativePath;
    if (!warPath.endsWith(".war")) {
      return null;
    }

    final String warUrl = build.url + "artifact/" + warPath;
    return warUrl;
  }

}
