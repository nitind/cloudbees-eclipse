package com.cloudbees.eclipse.ui.views.build;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuildDetailsResponse.ChangeSet.ChangeSetItem;

public class RecentChangesContentProvider implements ITreeContentProvider {

  private ChangeSetItem[] model;

  public void dispose() {
    this.model = null;
  }

  public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {

    if (newInput instanceof ChangeSetItem[]) {
      this.model = (ChangeSetItem[]) newInput;
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

    if (parentElement instanceof ChangeSetItem) {

      List<PathItem> ret = new ArrayList<RecentChangesContentProvider.PathItem>();
      ChangeSetItem el = (ChangeSetItem) parentElement;
      String[] added = el.addedPaths;
      String[] removed = el.deletedPaths;
      String[] modified = el.modifiedPaths;
      ChangeSetItem.ChangePath[] changes = el.paths;

      if (added != null) {
        for (int i = 0; i < added.length; i++) {
          ret.add(new PathItem(el, PathItem.TYPE.ADDED, added[i]));
        }
      }
      if (removed != null) {
        for (int i = 0; i < removed.length; i++) {
          ret.add(new PathItem(el, PathItem.TYPE.DELETED, removed[i]));
        }
      }
      if (modified != null) {
        for (int i = 0; i < modified.length; i++) {
          ret.add(new PathItem(el, PathItem.TYPE.MODIFIED, modified[i]));
        }
      }
      if (changes != null) {
        for (int i = 0; i < changes.length; i++) {
          ChangeSetItem.ChangePath change = changes[i];

          PathItem.TYPE type = PathItem.TYPE.MODIFIED;
          if ("add".equalsIgnoreCase(change.editType)) {
            type = PathItem.TYPE.ADDED;
          } else if ("delete".equalsIgnoreCase(change.editType)) {
            type = PathItem.TYPE.DELETED;
          }

          ret.add(new PathItem(el, type, change.file));
        }
      }
      return ret.toArray(new PathItem[0]);
    }
    return null;
  }

  public Object getParent(final Object element) {
    if (element instanceof PathItem) {
      return ((PathItem) element).parent;
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
    return false;
  }

  static class PathItem {
    enum TYPE {
      ADDED, DELETED, MODIFIED
    };

    TYPE type;
    String path;
    ChangeSetItem parent;

    public PathItem(final ChangeSetItem parent,
        final com.cloudbees.eclipse.ui.views.build.RecentChangesContentProvider.PathItem.TYPE added, final String string) {
      this.type = added;
      this.path = string;
      this.parent = parent;
    }

  }

  public ChangeSetItem[] getModel() {
    return this.model;
  }

}
