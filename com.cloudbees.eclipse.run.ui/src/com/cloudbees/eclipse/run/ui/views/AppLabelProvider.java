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

  private static final Image FOLDER = CBRunUiActivator.getImage(Images.CLOUDBEES_FOLDER);
  private static final Image TOMCAT = CBRunUiActivator.getImage(Images.CLOUDBEES_TOMCAT_ICON);

  @Override
  public String getText(final Object element) {
    if (element instanceof AppGroup) {
      return ((AppGroup) element).name;
    }

    if (element instanceof ApplicationInfo) {
      ApplicationInfo elem = (ApplicationInfo) element;
      return elem.getId() + " (" + elem.getStatus() + ")";
    }

    return null;
  }

  @Override
  public Image getImage(final Object element) {
    if (element instanceof AppGroup) {
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
