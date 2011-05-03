package com.cloudbees.eclipse.dev.ui.views.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.cloudbees.eclipse.core.forge.api.ForgeSync.ArtifactPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.Artifact;

public class ArtifactsContentProvider implements ITreeContentProvider {

  private JenkinsBuildDetailsResponse buildDetails;

  public void dispose() {
    this.buildDetails = null;
  }

  public JenkinsBuildDetailsResponse getBuildDetails() {
    return this.buildDetails;
  }

  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
    if (newInput instanceof JenkinsBuildDetailsResponse) {
      this.buildDetails = (JenkinsBuildDetailsResponse) newInput;
    } else {
      this.buildDetails = null;
    }
  }

  public Object[] getElements(final Object inputElement) {
    return getChildren(inputElement);
  }

  public Object[] getChildren(final Object parentElement) {
    if (parentElement instanceof String) {
      return new String[0];
    }

    if (parentElement == null || parentElement instanceof JenkinsBuildDetailsResponse) {
      Artifact[] artifacts = (this.buildDetails).artifacts;

      List<ArtifactPathItem> ret = new ArrayList<ArtifactPathItem>();
      if (artifacts != null) {
        for (Artifact art : artifacts) {
          ret.add(new ArtifactPathItem(this.buildDetails, art));
        }
      }

      return ret.toArray(new ArtifactPathItem[ret.size()]);
    }

    //    if (parentElement instanceof JenkinsBuildDetailsResponse.Artifact[]) {
    //      return (JenkinsBuildDetailsResponse.Artifact[]) parentElement;
    //    }
    //
    //    if (parentElement instanceof JenkinsBuildDetailsResponse.Artifact) {
    //      List<String> ret = new ArrayList<String>();
    //      ret.add(((JenkinsBuildDetailsResponse.Artifact) parentElement).relativePath);
    //      return ret.toArray(new String[ret.size()]);
    //    }

    System.out.println("Unknown parent: " + parentElement);

    return null;
  }

  public Object getParent(final Object element) {
    //    if (element instanceof ChangeSetPathItem) {
    //      return ((ChangeSetPathItem) element).parent;
    //    }
    return null;
  }

  public boolean hasChildren(final Object element) {

    Object[] children = getChildren(element);
    return children != null && children.length > 0;

    //    if (element instanceof ChangeSetItem) {
    //      ChangeSetItem item = (ChangeSetItem) element;
    //      if (item.addedPaths != null && item.addedPaths.length > 0) {
    //        return true;
    //      }
    //      if (item.deletedPaths != null && item.deletedPaths.length > 0) {
    //        return true;
    //      }
    //      if (item.modifiedPaths != null && item.modifiedPaths.length > 0) {
    //        return true;
    //      }
    //      if (item.paths != null && item.paths.length > 0) {
    //        return true;
    //      }
    //    }
    //    if (element instanceof ChangeSetItem[]) {
    //      return ((ChangeSetItem[]) element).length > 0;
    //    }
    //    if (element instanceof JenkinsBuildDetailsResponse) {
    //      return ((JenkinsBuildDetailsResponse) element).changeSet != null
    //      && ((JenkinsBuildDetailsResponse) element).changeSet.items != null
    //      && ((JenkinsBuildDetailsResponse) element).changeSet.items.length > 0;
    //    }
    //    return false;
  }

}
