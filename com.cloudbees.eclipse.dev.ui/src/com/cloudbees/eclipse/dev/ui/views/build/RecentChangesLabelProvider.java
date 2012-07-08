package com.cloudbees.eclipse.dev.ui.views.build;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.ui.TeamImages;

import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem;
import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem.TYPE;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Author;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem;
import com.cloudbees.eclipse.dev.ui.CBDEVImages;
import com.cloudbees.eclipse.dev.ui.CloudBeesDevUiPlugin;

public class RecentChangesLabelProvider extends LabelProvider {

  private Image imgAdded;
  private Image imgDeleted;
  private Image imgModified;
  private Image imgChangeSet;

  private ImageDescriptor IMG_DESC_CHANGESET = TeamImages.getImageDescriptor("obj/changeset_obj.gif");

  public RecentChangesLabelProvider() {
    super();
    this.imgAdded = new DecorationOverlayIcon(CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_FILE),
        CloudBeesDevUiPlugin.getImageDescription(CBDEVImages.IMG_FILE_ADDED), IDecoration.BOTTOM_RIGHT).createImage();
    this.imgDeleted = new DecorationOverlayIcon(CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_FILE),
        CloudBeesDevUiPlugin.getImageDescription(CBDEVImages.IMG_FILE_DELETED), IDecoration.BOTTOM_RIGHT).createImage();
    this.imgModified = new DecorationOverlayIcon(CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_FILE),
        CloudBeesDevUiPlugin.getImageDescription(CBDEVImages.IMG_FILE_MODIFIED), IDecoration.BOTTOM_RIGHT).createImage();
    this.imgChangeSet = this.IMG_DESC_CHANGESET.createImage();

  }

  @Override
  public String getText(final Object element) {
    if (element instanceof ChangeSetItem) {
      Author author = ((ChangeSetItem) element).author;
      String msg = ((ChangeSetItem) element).msg;// + " rev" + ((ChangeSetItem) element).rev;
      if (msg == null || msg.length() == 0) {
        msg = ((ChangeSetItem) element).comment;
      }
      if (msg == null || msg.length() == 0) {
        msg = "no message";
      }
      msg = msg.trim();
      String authorPart = "";
      if (author != null && author.fullName != null && author.fullName.length() > 0) {
        authorPart = "[" + author.fullName + "] ";
      }
      return authorPart + msg;
    }

    if (element instanceof ChangeSetPathItem) {
      String path = ((ChangeSetPathItem) element).path;
      return path;
    }
    return super.getText(element);
  }

  @Override
  public Image getImage(final Object element) {
    if (element instanceof ChangeSetItem) {
      return this.imgChangeSet;
    }
    if (element instanceof ChangeSetPathItem) {
      TYPE type = ((ChangeSetPathItem) element).type;
      if (type == TYPE.ADDED) {
        return this.imgAdded;
      }
      if (type == TYPE.MODIFIED) {
        return this.imgModified;
      }
      if (type == TYPE.DELETED) {
        return this.imgDeleted;
      }
      return CloudBeesDevUiPlugin.getImage(CBDEVImages.IMG_FILE);
    }

    return super.getImage(element);
  }

  @Override
  public void dispose() {
    this.imgChangeSet.dispose();
    this.imgAdded.dispose();
    this.imgDeleted.dispose();
    this.imgModified.dispose();
    this.imgChangeSet = null;
    this.imgAdded = null;
    this.imgDeleted = null;
    this.imgModified = null;
    super.dispose();
  }

}
