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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.cloudbees.eclipse.core.jenkins.api.ArtifactPathItem;
import com.cloudbees.eclipse.dev.ui.CBDEVImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;

public class ArtifactsLabelProvider extends LabelProvider {

  public ArtifactsLabelProvider() {
    super();
  }

  @Override
  public String getText(final Object element) {
    if (element instanceof ArtifactPathItem) {
      String path = ((ArtifactPathItem) element).item.relativePath;
      return path;
    }

    return super.getText(element);
  }

  @Override
  public Image getImage(final Object element) {
    if (element instanceof ArtifactPathItem) {
      if (((ArtifactPathItem) element).item.relativePath.endsWith(".war")) {
        return CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_DEPLOY);
      }
      return CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_FILE);
    }

    return super.getImage(element);
  }

  @Override
  public void dispose() {
    super.dispose();
  }

}
