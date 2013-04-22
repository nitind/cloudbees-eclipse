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
package com.cloudbees.eclipse.dev.ui.views.forge;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.dev.ui.CBDEVImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;

public class ForgeLabelProvider extends LabelProvider implements IFontProvider {

  public ForgeLabelProvider() {
    super();
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  @Override
  public String getText(final Object obj) {
    if (obj instanceof ForgeGroup) {
      ForgeGroup gr = (ForgeGroup) obj;

      return gr.isLoading() ? gr.getName() + " (loading)" : gr.getName();
    }

    if (obj instanceof ForgeInstance) {
      ForgeInstance inst = (ForgeInstance) obj;

      if (inst.status.equals(ForgeInstance.STATUS.SYNCED)) {
        return inst.url;
      }
      return inst.url + " (" + inst.status.getLabel().toLowerCase() + ")";
      
    }

    return null;
  }

  @Override
  public Image getImage(final Object obj) {
    if (obj instanceof ForgeGroup) {
      return CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_FOLDER_FORGE);
    }
    if (obj instanceof ForgeInstance) {
      if (((ForgeInstance) obj).type.equals(ForgeInstance.TYPE.GIT)) {
        return CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_INSTANCE_FORGE_GIT);
      } else {
        return CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_INSTANCE_FORGE_SVN);
      }
    }

    return null;
  }

  @Override
  public Font getFont(final Object obj) {
    if (obj instanceof ForgeGroup) {
      ForgeGroup gr = (ForgeGroup) obj;
      if (gr.isLoading()) {
        return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
      }
    }

    if (obj instanceof ForgeInstance) {
      ForgeInstance inst = (ForgeInstance) obj;
      if (inst.status.equals(ForgeInstance.STATUS.SYNCED)) {
        return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
      }
    }


    return null;
  }

}
