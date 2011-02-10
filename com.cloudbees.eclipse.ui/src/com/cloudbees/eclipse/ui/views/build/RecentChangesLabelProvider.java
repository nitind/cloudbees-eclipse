package com.cloudbees.eclipse.ui.views.build;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.ui.TeamImages;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.Author;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem;
import com.cloudbees.eclipse.ui.CBImages;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.build.RecentChangesContentProvider.PathItem.TYPE;

public class RecentChangesLabelProvider extends LabelProvider {

  private Image imgAdded;
  private Image imgDeleted;
  private Image imgModified;
  private Image imgChangeSet;

  private ImageDescriptor IMG_DESC_CHANGESET = TeamImages.getImageDescriptor("obj/changeset_obj.gif");

  public RecentChangesLabelProvider() {
    super();
    imgAdded = new DecorationOverlayIcon(CloudBeesUIPlugin.getImage(CBImages.IMG_FILE),
        CloudBeesUIPlugin.getImageDescription(CBImages.IMG_FILE_ADDED), IDecoration.BOTTOM_RIGHT).createImage();
    imgDeleted = new DecorationOverlayIcon(CloudBeesUIPlugin.getImage(CBImages.IMG_FILE),
        CloudBeesUIPlugin.getImageDescription(CBImages.IMG_FILE_DELETED), IDecoration.BOTTOM_RIGHT).createImage();
    imgModified = new DecorationOverlayIcon(CloudBeesUIPlugin.getImage(CBImages.IMG_FILE),
        CloudBeesUIPlugin.getImageDescription(CBImages.IMG_FILE_MODIFIED), IDecoration.BOTTOM_RIGHT).createImage();
    imgChangeSet = IMG_DESC_CHANGESET.createImage();

  }

  @Override
  public String getText(Object element) {
    if (element instanceof ChangeSetItem) {
      Author author = ((ChangeSetItem) element).author;
      String msg = ((ChangeSetItem) element).msg;// + " rev" + ((ChangeSetItem) element).rev;
      if (msg == null || msg.length() == 0) {
        msg = "no message";
      }
      String authorPart = "";
      if (author != null && author.fullName != null && author.fullName.length() > 0) {
        authorPart = "[" + author.fullName + "] ";
      }
      return authorPart + msg;
    }

    if (element instanceof RecentChangesContentProvider.PathItem) {
      String path = ((RecentChangesContentProvider.PathItem) element).path;
      return path;
    }
    return super.getText(element);
  }

  @Override
  public Image getImage(Object element) {
    if (element instanceof ChangeSetItem) {
      return imgChangeSet;
    }
    if (element instanceof RecentChangesContentProvider.PathItem) {
      TYPE type = ((RecentChangesContentProvider.PathItem) element).type;
      if (type == TYPE.ADDED) {
        return imgAdded;
      }
      if (type == TYPE.MODIFIED) {
        return imgModified;
      }
      if (type == TYPE.DELETED) {
        return imgDeleted;
      }
      return CloudBeesUIPlugin.getImage(CBImages.IMG_FILE);
    }

    return super.getImage(element);
  }

  @Override
  public void dispose() {
    imgChangeSet.dispose();
    imgAdded.dispose();
    imgDeleted.dispose();
    imgModified.dispose();
    imgChangeSet = null;
    imgAdded = null;
    imgDeleted = null;
    imgModified = null;
    super.dispose();
  }

}
