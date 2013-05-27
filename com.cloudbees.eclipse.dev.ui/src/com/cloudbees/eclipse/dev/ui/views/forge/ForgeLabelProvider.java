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

      String instUrl = inst.url;

      // string repo prefix
      if (instUrl != null) {

        if (instUrl.endsWith("/")) {
          instUrl = instUrl.substring(0, instUrl.length()-1);
        }
        
        if (instUrl.endsWith(".git")) {
          instUrl = instUrl.substring(0, instUrl.length()-4);
        }

        int idx = instUrl.lastIndexOf("/");
        if (idx > 0 && instUrl.length() > idx + 1) {
          instUrl = instUrl.substring(idx + 1);
        }
        
      }

      if (inst.status.equals(ForgeInstance.STATUS.SYNCED)) {
        return instUrl;
      }
      return instUrl + " (" + inst.status.getLabel().toLowerCase() + ")";

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
