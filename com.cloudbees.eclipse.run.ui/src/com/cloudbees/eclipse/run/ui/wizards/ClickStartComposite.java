package com.cloudbees.eclipse.run.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.gc.api.ClickStartTemplate;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.wizard.Failure;

public abstract class ClickStartComposite extends Composite {

  private static final String GROUP_LABEL = "ClickStart template";
  private static final String ERR_ADD_REPOS = "Please add SVN repositories to your CloudBees DEV@cloud";
  private static final String ERR_REPO_SELECTION = "SVN repository is not selected.";

  private ClickStartTemplate selectedTemplate;

  private TableViewer v;

  //private Button addTemplateCheck;
  //private Label templateLabel;

  //private Combo templateCombo;
  //private ComboViewer repoComboViewer;

  private IWizardContainer wizcontainer;

  private TemplateProvider templateProvider = new TemplateProvider();

  public ClickStartComposite(final Composite parent, IWizardContainer wizcontainer) {
    super(parent, SWT.NONE);
    this.wizcontainer = wizcontainer;
    init();
  }

  private void init() {

    FillLayout layout = new FillLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.spacing = 0;

    setLayout(layout);

    Group group = new Group(this, SWT.NONE);
    group.setText(GROUP_LABEL);
    group.setLayout(new GridLayout(2, false));

    GridData data = new GridData();
    data.horizontalSpan = 2;
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    data.horizontalAlignment = SWT.LEFT;

    /*   this.addTemplateCheck = new Button(group, SWT.CHECK);
       this.addTemplateCheck.setText(FORGE_REPO_CHECK_LABEL);
       this.addTemplateCheck.setSelection(false);
       this.addTemplateCheck.setLayoutData(data);
       this.addTemplateCheck.addSelectionListener(new MakeForgeRepoSelectionListener());

       data = new GridData();
       data.verticalAlignment = SWT.CENTER;

       this.templateLabel = new Label(group, SWT.NULL);
       this.templateLabel.setLayoutData(data);
       this.templateLabel.setText("Template:");
       this.templateLabel.setEnabled(false);

       data = new GridData();
       data.grabExcessHorizontalSpace = true;
       data.horizontalAlignment = SWT.FILL;

       this.templateCombo = new Combo(group, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
       this.templateCombo.setLayoutData(data);
       this.templateCombo.setEnabled(false);
       this.repoComboViewer = new ComboViewer(this.templateCombo);
       this.repoComboViewer.setLabelProvider(new TemplateLabelProvider());
       this.repoComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

         public void selectionChanged(final SelectionChangedEvent event) {
           ISelection selection = ClickStartComposite.this.repoComboViewer.getSelection();
           if (selection instanceof StructuredSelection) {
             ClickStartComposite.this.selectedTemplate = (ClickStartTemplate) ((StructuredSelection) selection)
                 .getFirstElement();
           }
           validate();
         }
       });*/
    /*

    Composite compositeJenkinsInstances = new Composite(group, SWT.NONE);
    compositeJenkinsInstances.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    GridLayout gl_compositeJenkinsInstances = new GridLayout(2, false);
    gl_compositeJenkinsInstances.marginWidth = 0;
    compositeJenkinsInstances.setLayout(gl_compositeJenkinsInstances);
    */
    Composite compositeTable = new Composite(group, SWT.NONE);
    compositeTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    GridLayout gl_compositeTable = new GridLayout(1, false);
    gl_compositeTable.marginHeight = 0;
    gl_compositeTable.marginWidth = 0;
    compositeTable.setLayout(gl_compositeTable);

    v = new TableViewer(compositeTable, SWT.BORDER | SWT.FULL_SELECTION);
    v.getTable().setLinesVisible(true);
    v.getTable().setHeaderVisible(true);
    v.setContentProvider(templateProvider);
    v.setInput("");

    v.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    ColumnViewerToolTipSupport.enableFor(v, ToolTip.NO_RECREATE);

    CellLabelProvider labelProvider = new CellLabelProvider() {

      public String getToolTipText(Object element) {
         ClickStartTemplate t = (ClickStartTemplate)element;
        return t.description;
      }

      public Point getToolTipShift(Object object) {
        return new Point(5, 5);
      }

      public int getToolTipDisplayDelayTime(Object object) {
        return 200;
      }

      public int getToolTipTimeDisplayed(Object object) {
        return 10000;
      }

      public void update(ViewerCell cell) {
        int idx = cell.getColumnIndex();
        ClickStartTemplate t = (ClickStartTemplate) cell.getElement();
        if (idx==0) {
          cell.setText(t.name);
        } else if (idx==1){
          String comps = "";
          
          for (int i = 0; i<t.components.length; i++) {
            comps = comps+t.components[i].name;
            if (i<t.components.length-1) {
              comps = comps+", ";
            }
          }
          cell.setText(comps);
        }
          
      }

    };

    /*    this.table = new Table(compositeTable, SWT.BORDER | SWT.FULL_SELECTION);
        this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);
    */
    v.getTable().addSelectionListener(new SelectionListener() {

      public void widgetSelected(final SelectionEvent e) {
        selectedTemplate = (ClickStartTemplate) e.item.getData();
        ClickStartComposite.this.fireTemplateChanged();
      }

      public void widgetDefaultSelected(final SelectionEvent e) {
        selectedTemplate = (ClickStartTemplate) e.item.getData();
        ClickStartComposite.this.fireTemplateChanged();
      }
    });

    //ColumnViewerToolTipSupport

    TableViewerColumn tblclmnLabel = new TableViewerColumn(v, SWT.NONE);
    tblclmnLabel.getColumn().setWidth(300);
    tblclmnLabel.getColumn().setText("Template");//TODO i18n
    tblclmnLabel.setLabelProvider(labelProvider);

    TableViewerColumn tblclmnUrl = new TableViewerColumn(v, SWT.NONE);
    tblclmnUrl.getColumn().setWidth(500);
    tblclmnUrl.getColumn().setText("Components");//TODO i18n
    tblclmnUrl.setLabelProvider(labelProvider);

    loadTable();
    v.getTable().setFocus();
  }

  protected void fireTemplateChanged() {
    System.out.println("Selected: "+selectedTemplate);
    validate();
  }

  public ClickStartTemplate getSelectedTemplate() {
    return this.selectedTemplate;
  }

  private void updateErrorStatus(String errorMsg) {
    //TODO Update!!
    //ClickStartTemplateWizardPage.this.updateErrorStatus(errorMsg);
  }

  private void validate() {
    Object[] tarr = templateProvider.getElements(null);
    if (tarr== null || tarr.length == 0) {
      updateErrorStatus(ERR_ADD_REPOS);
      setPageComplete(false);
      return;
    }

    if (getSelectedTemplate() == null) {
      updateErrorStatus(ERR_REPO_SELECTION);
      setPageComplete(false);
      return;
    }
    
    updateErrorStatus(null);
    setPageComplete(true);
  }

  abstract protected void setPageComplete(boolean b);

  private class TemplateLabelProvider extends LabelProvider {

    @Override
    public String getText(final Object element) {
      if (element instanceof ClickStartTemplate) {
        ClickStartTemplate repo = (ClickStartTemplate) element;
        return repo.name;
      }

      return super.getText(element);
    }

  }

  private void loadTable() {

    try {

      loadTableInternal();

    } catch (Exception e) {
      CBRunUiActivator.logError(e); // TODO
      //return new ClickStartTemplate[0];
    }

    /*    Collections.sort(insts, new Comparator<ClickStartTemplate>() {
          public int compare(final ClickStartTemplate o1, final ClickStartTemplate o2) {
            return o1.name.compareTo(o2.name);
          }
        });*/

  }

  private void loadTableInternal() {
    final List<ClickStartTemplate> repos = new ArrayList<ClickStartTemplate>();
    final Failure<CloudBeesException> failure = new Failure<CloudBeesException>();

    final IRunnableWithProgress operation = new IRunnableWithProgress() {

      public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          monitor.beginTask(" Loading ClickStart templates from the repository...", 0);

          Collection<ClickStartTemplate> retlist = CloudBeesCorePlugin.getDefault().getClickStartService()
              .loadTemplates(monitor);

          templateProvider.setElements(retlist.toArray(new ClickStartTemplate[0]));

          //repos.addAll(retlist);

          Display.getDefault().syncExec(new Runnable() {
            public void run() {
              v.refresh();
              ClickStartComposite.this.validate();
              //v.getTable().redraw();

              /*          
                        t.removeAll();
                        for (ClickStartTemplate instance : repos) {
                          TableItem tableItem = new TableItem(t, SWT.NONE);
                          String comps = "";
                          
                          for (int i = 0; i<instance.components.length; i++) {
                            comps = comps+instance.components[i].name;
                            if (i<instance.components.length-1) {
                              comps = comps+", ";
                            }
                          }
                          
                          tableItem.setText(new String[] { instance.name, comps });
                          tableItem.setData(instance);
                          
                        }

                        if (t.getItemCount() > 0) {
                          t.setSelection(0);
                        }
              */
            }
          });

        } catch (CloudBeesException e) {
          failure.cause = e;
        }
      }
    };
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        try {
          wizcontainer.run(true, false, operation);
        } catch (Exception e) {
          CBRunUiActivator.logError(e);
          e.printStackTrace();
        }
      }
    });
  }

  private static class TemplateProvider implements IStructuredContentProvider {

    private ClickStartTemplate[] elems = new ClickStartTemplate[] {};

    public TemplateProvider() {
    }

    public void setElements(ClickStartTemplate[] elems) {
      this.elems = elems;
    }

    public Object[] getElements(Object inputElement) {
      return elems;
    }

    public void dispose() {

    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

    }
  }
}
