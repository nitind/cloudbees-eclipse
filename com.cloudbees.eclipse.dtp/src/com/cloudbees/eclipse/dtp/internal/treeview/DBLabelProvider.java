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
package com.cloudbees.eclipse.dtp.internal.treeview;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.eclipse.dtp.CloudBeesDataToolsPlugin;
import com.cloudbees.eclipse.dtp.Images;

final class DBLabelProvider extends LabelProvider implements IFontProvider {

  private static final Image FOLDER = CloudBeesDataToolsPlugin.getImage(Images.CLOUDBEES_FOLDER);
  private static final Image DBICON = CloudBeesDataToolsPlugin.getImage(Images.JDBC_16_ICON);

  @Override
  public String getText(final Object element) {
    if (element instanceof DBGroup) {
      return ((DBGroup) element).name;
    }

    if (element instanceof DatabaseInfo) {
      DatabaseInfo elem = (DatabaseInfo) element;
      return elem.getName() + " (" + elem.getStatus()+")";
    }

    return null;
  }

  @Override
  public Image getImage(final Object element) {
    if (element instanceof DBGroup) {
      return FOLDER;
    }

    if (element instanceof DatabaseInfo) {
      return DBICON;
    }

    return null;
  }

  @Override
  public Font getFont(final Object obj) {
    if (obj instanceof DatabaseInfo) {
      DatabaseInfo appInfo = (DatabaseInfo) obj;
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
