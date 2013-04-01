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
package com.cloudbees.eclipse.ui.views;

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class CBLabelProvider extends LabelProvider implements IFontProvider {

  private CBTreeView owner;

  public CBLabelProvider(final CBTreeView owner) {
    this.owner = owner;
  }

  @Override
  public String getText(final Object obj) {
    for (ICBTreeProvider provider : this.owner.getProviders()) {
      if (provider.getLabelProvider()==null) {
        continue;
      }
      String text = provider.getLabelProvider().getText(obj);
      if (text != null) {
        return text;
      }
    }

    return null;
  }

  @Override
  public Image getImage(final Object obj) {
    for (ICBTreeProvider provider : this.owner.getProviders()) {
      if (provider.getLabelProvider()==null) {
        continue;
      }
      Image img = provider.getLabelProvider().getImage(obj);
      if (img != null) {
        return img;
      }
    }

    return null;
  }

  public Font getFont(final Object element) {
    for (ICBTreeProvider provider : this.owner.getProviders()) {
      if (provider.getLabelProvider()==null) {
        continue;
      }
      ILabelProvider labelProvider = provider.getLabelProvider();
      if (labelProvider instanceof IFontProvider) {
      Font font = ((IFontProvider) labelProvider).getFont(element);
      if (font != null) {
        return font;
      }
      }
    }

    return null;
  }

}
