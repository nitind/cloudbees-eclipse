package com.cloudbees.eclipse.dev.ui.views.instances;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;
import com.cloudbees.eclipse.dev.ui.CBImages;
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

      return inst.url + " (" + inst.status.name().toLowerCase() + ")";
    }

    return null;
  }

  @Override
  public Image getImage(final Object obj) {
    if (obj instanceof ForgeGroup) {
      return CloudBeesDevUiPlugin.getImage(CBImages.IMG_FOLDER_FORGE);
    }
    if (obj instanceof ForgeInstance) {
      return CloudBeesDevUiPlugin.getImage(CBImages.IMG_INSTANCE_FORGE);
    }

    return null;
  }

  public Font getFont(final Object obj) {
    if (obj instanceof ForgeGroup) {
      ForgeGroup gr = (ForgeGroup) obj;
      if (gr.isLoading()) {
        return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
      }
    }

    //    if (obj instanceof JenkinsInstanceResponse) {
    //      if (((JenkinsInstanceResponse) obj).offline) {
    //        return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
    //      }
    //    }

    return null;
  }

}
