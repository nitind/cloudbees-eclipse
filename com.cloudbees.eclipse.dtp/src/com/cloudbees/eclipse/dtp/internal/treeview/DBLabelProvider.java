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
