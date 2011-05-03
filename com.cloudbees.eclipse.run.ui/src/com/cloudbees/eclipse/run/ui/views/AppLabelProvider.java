package com.cloudbees.eclipse.run.ui.views;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.run.ui.Images;

final class AppLabelProvider extends LabelProvider implements IFontProvider {

  private static final Image FOLDER = CBRunUiActivator.imageDescriptorFromPlugin(CBRunUiActivator.PLUGIN_ID,
      Images.CLOUDBEES_FOLDER_PATH).createImage();

  private static final Image TOMCAT = CBRunUiActivator.imageDescriptorFromPlugin(CBRunUiActivator.PLUGIN_ID,
      Images.CLOUDBEES_TOMCAT_ICON_PATH).createImage();

  @Override
  public String getText(final Object element) {
    if (element instanceof String) {
      return (String) element;
    }

    if (element instanceof ApplicationInfo) {
      ApplicationInfo elem = (ApplicationInfo) element;
      return elem.getId() + " (" + elem.getStatus() + ")";
    }

    return null;
  }

  @Override
  public Image getImage(final Object element) {
    if (element instanceof String) {
      return FOLDER;
    }

    if (element instanceof ApplicationInfo) {
      return TOMCAT;
    }

    return null;
  }

  @Override
  public Font getFont(final Object obj) {
    if (obj instanceof ApplicationInfo) {
      ApplicationInfo appInfo = (ApplicationInfo) obj;
      String status = appInfo.getStatus();

      if (status.equals("active")) {
        return JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
      } else if (status.equals("stopped")) {
        return JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT);
      }
    }

    return null;
  }

}
