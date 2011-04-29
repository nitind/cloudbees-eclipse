package com.cloudbees.eclipse.dev.ui.views.instances;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse.View;
import com.cloudbees.eclipse.dev.ui.CBImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class InstanceLabelProvider extends LabelProvider implements IFontProvider {


  public InstanceLabelProvider() {
    super();
  }

  @Override
  public void dispose() {
    super.dispose();
  }


  @Override
  public String getText(final Object obj) {
    if (obj instanceof InstanceGroup) {
      InstanceGroup gr = (InstanceGroup) obj;

      return gr.isLoading() ? gr.getName() + " (loading)" : gr.getName();
    }

    if (obj instanceof JenkinsInstanceResponse) {
      JenkinsInstanceResponse inst = (JenkinsInstanceResponse) obj;

      JenkinsService s = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(inst.viewUrl);
      if (s != null) {
        return s.getLabel() + (inst.offline ? " (offline)" : "");//TODO i18n
      }

      String name = inst.nodeName;
      if (name != null && name.length() > 0) {
        return name + (inst.offline ? " (offline)" : "");//TODO i18n
      }
      return inst.primaryView.url + (inst.offline ? " (offline)" : "");//TODO i18n
    }
    if (obj instanceof JenkinsInstanceResponse.View) {
      JenkinsInstanceResponse.View view = (View) obj;
      return view.name;
    }
    return null;
  }

  @Override
  public Image getImage(final Object obj) {
    if (obj instanceof FavoritesInstanceGroup) {
      return CloudBeesDevUiPlugin.getImage(CBImages.IMG_VIEW);
    } else if (obj instanceof InstanceGroup) {
      //return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.);
      if (((InstanceGroup) obj).isCloudHosted()) {
        return CloudBeesDevUiPlugin.getImage(CBImages.IMG_FOLDER_HOSTED);
        //imgFolderHosted;
      }
      return CloudBeesDevUiPlugin.getImage(CBImages.IMG_FOLDER_LOCAL);
    }

    if (obj instanceof JenkinsInstanceResponse) {
      return CloudBeesDevUiPlugin.getImage(CBImages.IMG_INSTANCE);
    }
    if (obj instanceof JenkinsInstanceResponse.View) {
      return CloudBeesDevUiPlugin.getImage(CBImages.IMG_VIEW);
    }

    return null;
  }

  public Font getFont(final Object obj) {
    if (obj instanceof InstanceGroup) {
      InstanceGroup gr = (InstanceGroup) obj;
      if (gr.isLoading()) {
        return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
      }
    }

    if (obj instanceof JenkinsInstanceResponse.View) {
      View view = (JenkinsInstanceResponse.View) obj;
      if (view.isPrimary) {
        return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
      }
    } else if (obj instanceof JenkinsInstanceResponse) {
      if (((JenkinsInstanceResponse) obj).offline) {
        return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
      }
    }
    return null;
  }

}
