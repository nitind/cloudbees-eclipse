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
      Image img = provider.getLabelProvider().getImage(obj);
      if (img != null) {
        return img;
      }
    }

    return null;
  }

  public Font getFont(final Object element) {
    for (ICBTreeProvider provider : this.owner.getProviders()) {
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
