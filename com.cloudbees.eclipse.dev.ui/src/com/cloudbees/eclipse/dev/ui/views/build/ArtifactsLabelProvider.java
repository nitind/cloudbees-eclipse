/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
