package com.cloudbees.eclipse.dev.ui.views.jobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

public class JobsContentProvider extends BaseWorkbenchContentProvider {
  private List<JobHolder> root;

  private List<Object> expList = new ArrayList<Object>();

  private DeferredTreeContentManager manager;

  private final static Object[] EMPTY_ARRAY = new Object[0];

  private IViewSite viewSite;

  public JobsContentProvider(IViewSite viewSite) {
    super();
    this.viewSite = viewSite;
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    if (newInput instanceof List && (((List) newInput).isEmpty() || ((List) newInput).get(0) instanceof JobHolder)) {
      root = (List<JobHolder>) newInput;
    } else {
      root = null; // reset
    }

    if (viewer instanceof AbstractTreeViewer) {
      manager = new DeferredTreeContentManager((AbstractTreeViewer) viewer, viewSite) {
        @Override
        protected IDeferredWorkbenchAdapter getAdapter(Object element) {
          IDeferredWorkbenchAdapter ret = super.getAdapter(element);
          if (ret != null) {
            return ret;
          }

          if (element instanceof JobHolder) {
            if (((JobHolder) element).job.isFolderOrView()) {
              return new DeferWrapper((JobHolder) element);
            }
          }

          return null;
        }

      };

      final AbstractTreeViewer treeViewer = ((AbstractTreeViewer) viewer);

      Object[] expelems = ((AbstractTreeViewer) viewer).getVisibleExpandedElements();
      expList.clear();
      expList.addAll(Arrays.asList(expelems));

      IJobChangeListener listener = new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent event) {
          if (event.getResult().isOK()) {
            // try to expand the state again
            PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
              public void run() {

                if (expList.size() == 0) {
                  // all good, managed to expand all previous nodes
                  return;
                }

                for (Object o : expList) {
                  treeViewer.setExpandedState(o, true);
                }

                List<Object> toremove = new ArrayList<Object>();
                for (Object o : expList) {
                  if (treeViewer.getExpandedState(o)) {
                    toremove.add(o);
                  }
                }
                expList.removeAll(toremove);

              }
            });
          }
        }
      };

      manager.addUpdateCompleteListener(listener);

    }

  }

  public void dispose() {
  }

  public Object[] getElements(Object parent) {
    return getChildren(parent);
  }

  @Override
  public Object[] getChildren(final Object parent) {

    if (parent instanceof IViewSite) {
      if (root == null) {
        return new JobHolder[0];
      } else {
        return root.toArray(new JobHolder[root.size()]);
      }
    }

    if (manager != null) {
      Object[] children = manager.getChildren(parent);
      if (children != null) {
        return children;
      }
    }

    return EMPTY_ARRAY;
  }

  public Object getParent(Object element) {
    // not used for now so we won't track it
    return null;
  }

  @Override
  public boolean hasChildren(Object element) {

    if (manager != null) {
      if (manager.isDeferredAdapter(element))
        return manager.mayHaveChildren(element);
    }

    // only views or folders coming from the deferred manager can have children

    return false;
  }

  public void removeDeferredExpanders(Object element) {
    Iterator<Object> it = expList.iterator();
    List<Object> toremove = new ArrayList<Object>();
    while (it.hasNext()) {
      toremove.add(it.next());
    }
    expList.removeAll(toremove);
  }

}
