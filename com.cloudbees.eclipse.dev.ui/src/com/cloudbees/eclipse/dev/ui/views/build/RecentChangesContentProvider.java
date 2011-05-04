package com.cloudbees.eclipse.dev.ui.views.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.cloudbees.eclipse.core.jenkins.api.ChangeSetPathItem;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem;

public class RecentChangesContentProvider implements ITreeContentProvider {

  private JenkinsBuildDetailsResponse buildDetails;
  private ChangeSetItem[] model;

  public void dispose() {
    this.model = null;
    this.buildDetails = null;
  }

  public JenkinsBuildDetailsResponse getBuildDetails() {
    return this.buildDetails;
  }

  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {

    if (newInput instanceof ChangeSetItem[]) {
      this.model = (ChangeSetItem[]) newInput;
    } else if (newInput instanceof JenkinsBuildDetailsResponse) {
      this.buildDetails = (JenkinsBuildDetailsResponse) newInput;
      this.model = ((JenkinsBuildDetailsResponse) newInput).changeSet.items;
    }

  }

  public Object[] getElements(final Object inputElement) {
    return getChildren(inputElement);
  }

  public Object[] getChildren(final Object parentElement) {
    if (parentElement == null) {
      return this.model;
    }

    if (parentElement instanceof String) {
      return new String[0];
    }

    if (parentElement instanceof ChangeSetItem[]) {
      return (ChangeSetItem[]) parentElement;
    }

    if (parentElement instanceof JenkinsBuildDetailsResponse) {
      return ((JenkinsBuildDetailsResponse) parentElement).changeSet.items;
    }

    if (parentElement instanceof ChangeSetItem) {
      List<ChangeSetPathItem> ret = new ArrayList<ChangeSetPathItem>();
      ChangeSetItem el = (ChangeSetItem) parentElement;
      String[] added = el.addedPaths;
      String[] removed = el.deletedPaths;
      String[] modified = el.modifiedPaths;
      ChangeSetItem.ChangePath[] changes = el.paths;

      if (added != null) {
        for (int i = 0; i < added.length; i++) {
          ret.add(new ChangeSetPathItem(el, ChangeSetPathItem.TYPE.ADDED, added[i]));
        }
      }
      if (removed != null) {
        for (int i = 0; i < removed.length; i++) {
          ret.add(new ChangeSetPathItem(el, ChangeSetPathItem.TYPE.DELETED, removed[i]));
        }
      }
      if (modified != null) {
        for (int i = 0; i < modified.length; i++) {
          ret.add(new ChangeSetPathItem(el, ChangeSetPathItem.TYPE.MODIFIED, modified[i]));
        }
      }
      if (changes != null) {
        for (int i = 0; i < changes.length; i++) {
          ChangeSetItem.ChangePath change = changes[i];

          ChangeSetPathItem.TYPE type = ChangeSetPathItem.TYPE.MODIFIED;
          if ("add".equalsIgnoreCase(change.editType)) {
            type = ChangeSetPathItem.TYPE.ADDED;
          } else if ("delete".equalsIgnoreCase(change.editType)) {
            type = ChangeSetPathItem.TYPE.DELETED;
          }

          ret.add(new ChangeSetPathItem(el, type, change.file));
        }
      }
      return ret.toArray(new ChangeSetPathItem[ret.size()]);
    }

    System.out.println("Unknown parent: " + parentElement);

    return null;
  }

  public Object getParent(final Object element) {
    if (element instanceof ChangeSetPathItem) {
      return ((ChangeSetPathItem) element).parent;
    }
    return null;
  }

  public boolean hasChildren(final Object element) {

    if (element instanceof ChangeSetItem) {
      ChangeSetItem item = (ChangeSetItem) element;
      if (item.addedPaths != null && item.addedPaths.length > 0) {
        return true;
      }
      if (item.deletedPaths != null && item.deletedPaths.length > 0) {
        return true;
      }
      if (item.modifiedPaths != null && item.modifiedPaths.length > 0) {
        return true;
      }
      if (item.paths != null && item.paths.length > 0) {
        return true;
      }
    }
    if (element instanceof ChangeSetItem[]) {
      return ((ChangeSetItem[]) element).length > 0;
    }
    if (element instanceof JenkinsBuildDetailsResponse) {
      return ((JenkinsBuildDetailsResponse) element).changeSet != null
      && ((JenkinsBuildDetailsResponse) element).changeSet.items != null
      && ((JenkinsBuildDetailsResponse) element).changeSet.items.length > 0;
    }
    return false;
  }

  public ChangeSetItem[] getModel() {
    return this.model;
  }

}
