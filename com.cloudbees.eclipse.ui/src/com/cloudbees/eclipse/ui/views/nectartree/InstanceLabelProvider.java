package com.cloudbees.eclipse.ui.views.nectartree;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.cloudbees.eclipse.core.NectarService;
import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarInstanceResponse.View;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class InstanceLabelProvider extends LabelProvider implements IFontProvider {

  private final static ImageDescriptor ICON_FOLDER_HOSTED = ImageDescriptor.createFromURL(CloudBeesUIPlugin
      .getDefault().getBundle().getResource("/icons/16x16/cb_folder_run.png"));

  private final static ImageDescriptor ICON_FOLDER_LOCAL = ImageDescriptor.createFromURL(CloudBeesUIPlugin.getDefault()
      .getBundle().getResource("/icons/16x16/cb_folder_run.png"));

  private final static ImageDescriptor ICON_INSTANCE = ImageDescriptor.createFromURL(CloudBeesUIPlugin.getDefault()
      .getBundle().getResource("/icons/16x16/jenkins.png"));

  private final static ImageDescriptor ICON_VIEW = ImageDescriptor.createFromURL(CloudBeesUIPlugin.getDefault()
      .getBundle().getResource("/icons/16x16/cb_view_dots_big.png"));

  private Image imgFolderHosted;
  private Image imgFolderLocal;
  private Image imgInstance;
  private Image imgView;

  public InstanceLabelProvider() {

    imgFolderHosted = ICON_FOLDER_HOSTED.createImage();
    imgFolderLocal = ICON_FOLDER_LOCAL.createImage();
    imgInstance = ICON_INSTANCE.createImage();
    imgView = ICON_VIEW.createImage();
  }

  @Override
  public void dispose() {
    imgFolderHosted.dispose();
    imgFolderHosted = null;
    imgFolderLocal.dispose();
    imgFolderLocal = null;
    imgInstance.dispose();
    imgInstance = null;
    imgView.dispose();
    imgView = null;

    super.dispose();
  }
  

  public String getText(Object obj) {
    if (obj instanceof InstanceGroup) {
      InstanceGroup gr = (InstanceGroup) obj;
      return gr.getName();
    }
    if (obj instanceof NectarInstanceResponse) {
      NectarInstanceResponse inst = (NectarInstanceResponse) obj;

      NectarService s = CloudBeesUIPlugin.getDefault().getNectarServiceForUrl(inst.serviceUrl);
      if (s != null) {
        return s.getLabel();
      }

      String name = inst.nodeName;
      if (name != null && name.length() > 0) {
        return name;
      }
      return inst.primaryView.url;
    }
    if (obj instanceof NectarInstanceResponse.View) {
      NectarInstanceResponse.View view = (View) obj;
      return view.name;
    }
    return obj.toString();
  }

  public Image getImage(Object obj) {
    if (obj instanceof InstanceGroup) {
      //return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.);
      if (((InstanceGroup) obj).isCloudHosted()) {
              return imgFolderHosted;
            }
            return imgFolderLocal;

    }

    if (obj instanceof NectarInstanceResponse) {
      return imgInstance;
    }
    if (obj instanceof NectarInstanceResponse.View) {
      return imgView;
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
    if (element instanceof NectarInstanceResponse.View) {
      View view = (NectarInstanceResponse.View) element;
      if (view.isPrimary) {
        return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
      }
    }
    return null;
  }

}
