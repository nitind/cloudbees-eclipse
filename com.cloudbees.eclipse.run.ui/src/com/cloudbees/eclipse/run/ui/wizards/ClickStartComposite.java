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
package com.cloudbees.eclipse.run.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

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
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.gc.api.ClickStartTemplate;
import com.cloudbees.eclipse.core.gc.api.ClickStartTemplate.Component;
import com.cloudbees.eclipse.dev.scm.egit.ForgeEGitSync;
import com.cloudbees.eclipse.run.ui.CBRunUiActivator;
import com.cloudbees.eclipse.ui.AuthStatus;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.jcraft.jsch.JSchException;

public abstract class ClickStartComposite extends Composite {

  private static final String GROUP_LABEL = "ClickStart template";
  private static final String ERR_TEMPLATES_NOT_FOUND = "No ClickStart templates found.";
  private static final String ERR_TEMPLATE_SELECTION = "Please select a ClickStart template to get started.";

  private ClickStartTemplate selectedTemplate;

  private TableViewer v;

  //private Button addTemplateCheck;
  //private Label templateLabel;

  //private Combo templateCombo;
  //private ComboViewer repoComboViewer;

  private IWizardContainer wizcontainer;

  private TemplateProvider templateProvider = new TemplateProvider();
  private StyledText dlabel;
  private Browser browser;
  private String bgStr;

  public ClickStartComposite(final Composite parent, IWizardContainer wizcontainer) {
    super(parent, SWT.NONE);
    this.wizcontainer = wizcontainer;
    init();
  }

  private void init() {

    GridLayout layout = new GridLayout(1, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.horizontalSpacing = 0;
    layout.verticalSpacing = 0;
    layout.marginTop = 10;
    setLayout(layout);

    GridData d1 = new GridData();
    d1.horizontalSpan = 1;
    d1.grabExcessHorizontalSpace = true;
    d1.grabExcessVerticalSpace = true;
    d1.horizontalAlignment = SWT.FILL;
    d1.verticalAlignment = SWT.FILL;
    setLayoutData(d1);

    Group group = new Group(this, SWT.FILL);
    group.setText(GROUP_LABEL);

    GridLayout grl = new GridLayout(1, false);
    grl.horizontalSpacing = 0;
    grl.verticalSpacing = 0;
    grl.marginHeight = 0;
    grl.marginWidth = 0;
    grl.marginTop = 4;
    group.setLayout(grl);

    GridData data = new GridData();
    data.horizontalSpan = 1;
    data.grabExcessHorizontalSpace = true;
    data.grabExcessVerticalSpace = true;
    data.horizontalAlignment = SWT.FILL;
    data.verticalAlignment = SWT.FILL;
    group.setLayoutData(data);

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

    v.getTable().setLayout(new GridLayout(1, false));

    GridData vgd = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
    v.getTable().setLayoutData(vgd);
    ColumnViewerToolTipSupport.enableFor(v, ToolTip.NO_RECREATE);

    CellLabelProvider labelProvider = new CellLabelProvider() {

      public String getToolTipText(Object element) {
        ClickStartTemplate t = (ClickStartTemplate) element;
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
        if (idx == 0) {
          cell.setText(t.name);
        } else if (idx == 1) {
          String comps = "";

          for (int i = 0; i < t.components.length; i++) {
            comps = comps + t.components[i].name;
            if (i < t.components.length - 1) {
              comps = comps + ", ";
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
    tblclmnUrl.getColumn().setWidth(800);
    tblclmnUrl.getColumn().setText("Components");//TODO i18n
    tblclmnUrl.setLabelProvider(labelProvider);

    loadData();

    //Group group2 = new Group(this, SWT.NONE);
    //group2.setText("");
    //group2.setLayout(ld2);
    GridData data2 = new GridData();
    data2.horizontalSpan = 1;
    data2.grabExcessHorizontalSpace = true;
    //data2.grabExcessVerticalSpace = true;
    data2.horizontalAlignment = SWT.FILL;
    //data2.verticalAlignment = SWT.FILL;
    //group2.setLayoutData(data2);

    browser = new Browser(this, SWT.NONE);
    //browser.getVerticalBar().setVisible(false);
    //browser.getHorizontalBar().setVisible(false);

    GridLayout ld2 = new GridLayout(2, true);
    ld2.horizontalSpacing = 0;
    ld2.verticalSpacing = 0;
    ld2.marginHeight = 0;
    ld2.marginWidth = 0;

    GridData gd2 = new GridData(SWT.FILL, SWT.FILL);
    gd2.heightHint = 50;
    gd2.horizontalSpan = 1;
    gd2.grabExcessHorizontalSpace = true;
    gd2.grabExcessVerticalSpace = false;
    gd2.horizontalAlignment = SWT.FILL;

    browser.setLayout(ld2);
    browser.setLayoutData(gd2);

    Color bg = this.getBackground();
    bgStr = "rgb(" + bg.getRed() + "," + bg.getGreen() + "," + bg.getBlue() + ")";

    browser.setText("<html><head><style>body{background-color:" + bgStr
        + ";margin:0px;padding:0px;width:100%;}</style></head><body style='overflow:hidden;'></body></html>");

    //shell.open();

    //browser.setUrl("https://google.com");

    browser.addLocationListener(new LocationListener() {

      @Override
      public void changing(LocationEvent event) {
        String url = event.location;
        try {
          if (url != null && url.startsWith("http")) {
            event.doit = false;
            PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
          }
        } catch (PartInitException e) {
          e.printStackTrace();
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void changed(LocationEvent event) {
        //event.doit = false;
      }
    });

    v.getTable().setFocus();

    /*    getParent().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
        setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        group.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_CYAN));
        v.getTable().setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
        browser.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW));
    */}

  private Exception loadData() {

    if (AuthStatus.OK != CloudBeesUIPlugin.getDefault().getAuthStatus()) {
      ClickStartComposite.this
          .updateErrorStatus("User is not authenticated. Please review CloudBees account settings.");
      return null;
    }

    final Exception[] ex = { null };

    final IRunnableWithProgress operation1 = new IRunnableWithProgress() {

      public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          monitor.beginTask(" Loading ClickStart templates from the repository...", 0);

          Collection<ClickStartTemplate> retlist = CloudBeesCorePlugin.getDefault().getClickStartService()
              .loadTemplates(monitor);

          templateProvider.setElements(retlist.toArray(new ClickStartTemplate[0]));

          Display.getDefault().syncExec(new Runnable() {
            public void run() {
              v.refresh();
              ClickStartComposite.this.validate();
            }
          });

        } catch (CloudBeesException e) {
          ex[0] = e;
        }
      }
    };

    final IRunnableWithProgress operation2 = new IRunnableWithProgress() {

      public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
          monitor.beginTask(" Testing your connection to ssh://git.cloudbees.com...", 0);

          try {
            if (!ForgeEGitSync.validateSSHConfig(monitor)) {
              ex[0] = new CloudBeesException("Failed to connect!");
            }
          } catch (JSchException e) {
            ex[0] = e;
          }

        } catch (CloudBeesException e) {
          ex[0] = e;
        }
      }
    };
    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        try {
          wizcontainer.run(true, false, operation2);
          if (ex[0] == null) {
            wizcontainer.run(true, false, operation1);
          }

          if (ex[0] != null) {
            //ex[0].printStackTrace();

            if ("Auth fail".equals(ex[0].getMessage())) {
              ClickStartComposite.this.updateErrorStatus("Authentication failed. Are SSH keys properly configured?");
            } else {
              ClickStartComposite.this.updateErrorStatus(ex[0].getMessage());
            }

            ClickStartComposite.this.setPageComplete(ex[0] == null);
          }
        } catch (Exception e) {
          CBRunUiActivator.logError(e);
          e.printStackTrace();
        }
      }
    });

    return ex[0];

  }

  protected void fireTemplateChanged() {
    //System.out.println("Selected: " + selectedTemplate);
    String style = "<html><head><style>a:visited{color:#0000A0};a{color:#0000A0};body{width:100%;overflow:auto;font-family:tahoma,verdana,arial;font-size:12px;margin:0px;margin-left:5px;margin-top:5px;padding:0px;background-color:"
        + bgStr + "}#descr{position:absolute;top:7px;left:60px}</style></head><body>";
    String comps = "<br/><br/>";

    for (int i = 0; i < selectedTemplate.components.length; i++) {
      Component c = selectedTemplate.components[i];
      comps = comps + "<img style='height:25px;width:auto;' alt='"+c.name+" ("+c.description+")' src='"+c.icon+"'/>";
      if (i<selectedTemplate.components.length-1) {
        comps=comps+" ";
      }
    }
    
    String docs = "";
    if (selectedTemplate.docUrl != null && selectedTemplate.docUrl.length() > 0) {
      docs = ", <a href='" + selectedTemplate.docUrl + "' title='"+selectedTemplate.docUrl+"'>Documentation</a>";
    }

    String txt = style + "<img style='height:38px; width:auto;' src='" + selectedTemplate.icon
        + "'/><div id='descr'><b>" + selectedTemplate.name + "</b>" + docs + "<br/>" + selectedTemplate.description
         + "</div></body></html>";
    //dlabel.setText(txt);
    browser.setText(txt, false);
    validate();
  }

  public ClickStartTemplate getSelectedTemplate() {
    return this.selectedTemplate;
  }

  abstract protected void updateErrorStatus(String errorMsg);

  private void validate() {
    Object[] tarr = templateProvider.getElements(null);
    if (tarr == null || tarr.length == 0) {
      updateErrorStatus(ERR_TEMPLATES_NOT_FOUND);
      setPageComplete(false);
      return;
    }

    if (getSelectedTemplate() == null) {
      updateErrorStatus(ERR_TEMPLATE_SELECTION);
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

  @Override
  public void dispose() {
    browser.close();
    browser.dispose();
    browser = null;
    super.dispose();
  }

}
