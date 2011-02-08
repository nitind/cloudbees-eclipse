package com.cloudbees.eclipse.ui.views.instances;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.cloudbees.eclipse.core.JenkinsService;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsInstanceResponse.View;
import com.cloudbees.eclipse.ui.CBImages;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class InstanceLabelProvider extends LabelProvider implements IFontProvider {


  public InstanceLabelProvider() {
    super();
  }

  @Override
  public void dispose() {
    super.dispose();
  }
  

  public String getText(Object obj) {
    if (obj instanceof InstanceGroup) {
      InstanceGroup gr = (InstanceGroup) obj;
      return gr.getName();
    }
    if (obj instanceof JenkinsInstanceResponse) {
      JenkinsInstanceResponse inst = (JenkinsInstanceResponse) obj;

      JenkinsService s = CloudBeesUIPlugin.getDefault().getJenkinsServiceForUrl(inst.serviceUrl);
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
    return obj.toString();
  }

  public Image getImage(Object obj) {
    if (obj instanceof InstanceGroup) {
      //return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.);
      if (((InstanceGroup) obj).isCloudHosted()) {
        return CloudBeesUIPlugin.getImage(CBImages.IMG_FOLDER_HOSTED);
        //imgFolderHosted;
            }
      return CloudBeesUIPlugin.getImage(CBImages.IMG_FOLDER_LOCAL);

    }

    if (obj instanceof JenkinsInstanceResponse) {
      return CloudBeesUIPlugin.getImage(CBImages.IMG_INSTANCE);
    }
    if (obj instanceof JenkinsInstanceResponse.View) {
      return CloudBeesUIPlugin.getImage(CBImages.IMG_VIEW);
    }

    return null;
    //String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
    /*    String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
        if (obj instanceof InstanceGroup) {
          imageKey = ISharedImages.IMG_OBJ_PROJECT;
        }
        return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);*/
  }

  public Font getFont(Object element) {
    if (element instanceof JenkinsInstanceResponse.View) {
      View view = (JenkinsInstanceResponse.View) element;
      if (view.isPrimary) {
        return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
      }
    } else if (element instanceof JenkinsInstanceResponse) {
      if (((JenkinsInstanceResponse) element).offline) {
        return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
      }
    }
    return null;
  }

}
