package com.cloudbees.eclipse.ui.views.build;

import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.NectarService;
import com.cloudbees.eclipse.core.nectar.api.NectarBuildDetailsResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarBuildDetailsResponse.ChangeSet.ChangeSetItem;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class BuildPart extends EditorPart {

  public static final String ID = "com.cloudbees.eclipse.ui.views.build.BuildPart"; //$NON-NLS-1$
  private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

  private NectarBuildDetailsResponse detail;

  private boolean lastBuildAvailable = false;
  private ScrolledForm form;
  private Label textTopSummary;
  private Composite composite_1;
  private Label contentBuildSummary;
  private Label contentBuildHistory;
  private Label contentJUnitTests;
  private Label contentRecentChanges;

  public BuildPart() {
    super();
  }

  /**
   * Create contents of the editor part.
   * 
   * @param parent
   */
  @Override
  public void createPartControl(Composite parent) {

    System.out.println("Creating control");

    form = formToolkit.createScrolledForm(parent);
    formToolkit.decorateFormHeading(form.getForm());
    formToolkit.paintBordersFor(form);
    form.setText("n/a");

    ColumnLayout columnLayout = new ColumnLayout();
    columnLayout.maxNumColumns = 1;
    form.getBody().setLayout(columnLayout);

    textTopSummary = formToolkit.createLabel(form.getBody(), "n/a", SWT.BOLD);
    
    Composite composite = formToolkit.createComposite(form.getBody(), SWT.NONE);
    formToolkit.paintBordersFor(composite);
    ColumnLayout cl_composite = new ColumnLayout();
    cl_composite.bottomMargin = 0;
    cl_composite.horizontalSpacing = 10;
    cl_composite.rightMargin = 0;
    cl_composite.verticalSpacing = 10;
    cl_composite.leftMargin = 0;
    cl_composite.minNumColumns = 2;
    cl_composite.maxNumColumns = 2;
    composite.setLayout(cl_composite);

    Section sectSummary = formToolkit.createSection(composite, Section.TITLE_BAR);
    formToolkit.paintBordersFor(sectSummary);
    sectSummary.setText("Build Summary");

    composite_1 = new Composite(sectSummary, SWT.NONE);
    formToolkit.adapt(composite_1);
    formToolkit.paintBordersFor(composite_1);
    sectSummary.setClient(composite_1);
    ColumnLayout cl_composite_1 = new ColumnLayout();
    cl_composite_1.maxNumColumns = 1;
    composite_1.setLayout(cl_composite_1);

    contentBuildSummary = formToolkit.createLabel(composite_1, "n/a", SWT.NONE);

    Section sectBuildHistory = formToolkit.createSection(composite, Section.TITLE_BAR);
    formToolkit.paintBordersFor(sectBuildHistory);
    sectBuildHistory.setText("Build History");

    Composite composite_2 = new Composite(sectBuildHistory, SWT.NONE);
    formToolkit.adapt(composite_2);
    formToolkit.paintBordersFor(composite_2);
    sectBuildHistory.setClient(composite_2);
    ColumnLayout cl_composite_2 = new ColumnLayout();
    cl_composite_2.maxNumColumns = 1;
    composite_2.setLayout(cl_composite_2);

    contentBuildHistory = formToolkit.createLabel(composite_2, "n/a", SWT.NONE);

    Section sectTests = formToolkit.createSection(composite, Section.TITLE_BAR);
    formToolkit.paintBordersFor(sectTests);
    sectTests.setText("JUnit Tests");

    Composite composite_4 = new Composite(sectTests, SWT.NONE);
    formToolkit.adapt(composite_4);
    formToolkit.paintBordersFor(composite_4);
    sectTests.setClient(composite_4);
    ColumnLayout cl_composite_4 = new ColumnLayout();
    cl_composite_4.maxNumColumns = 1;
    composite_4.setLayout(cl_composite_4);

    contentJUnitTests = formToolkit.createLabel(composite_4, "n/a", SWT.NONE);

    Section sectRecentChanges = formToolkit.createSection(composite, Section.TITLE_BAR);
    formToolkit.paintBordersFor(sectRecentChanges);
    sectRecentChanges.setText("Changes");

    Composite composite_3 = new Composite(sectRecentChanges, SWT.NONE);
    formToolkit.adapt(composite_3);
    formToolkit.paintBordersFor(composite_3);
    sectRecentChanges.setClient(composite_3);
    ColumnLayout cl_composite_3 = new ColumnLayout();
    cl_composite_3.maxNumColumns = 1;
    composite_3.setLayout(cl_composite_3);

    contentRecentChanges = formToolkit.createLabel(composite_3, "n/a", SWT.NONE);

    Action haction = new Action("hor", Action.AS_PUSH_BUTTON) { //$NON-NLS-1$
      public void run() {
        //form.reflow(true);
      }
    };
    //haction.setChecked(true);
    haction.setText("HEI");
    haction.setToolTipText("TOOLTIP"); //$NON-NLS-1$
    haction.setImageDescriptor(CloudBeesUIPlugin.getDefault().getImageRegistry()
        .getDescriptor(ISharedImages.IMG_OBJ_ELEMENT));


    //IToolBarManager tbm = getEditorSite().getPage()

    //tbm.add(haction);
    form.getToolBarManager().add(haction);
    form.getToolBarManager().add(new Separator());
    form.getToolBarManager().update(false);



    loadData();

  }

  public IEditorSite getEditorSite() {
    return (IEditorSite) getSite();
  }

  private void loadData() {
    if (getEditorInput() == null) {
      return;
    }

    BuildEditorInput details = (BuildEditorInput) getEditorInput();

    this.lastBuildAvailable = false;

    if (details == null || details.getLastBuild() == null || details.getLastBuild().url == null) {
      // No last build available
    } else {

      NectarService service = CloudBeesUIPlugin.getDefault().getNectarServiceForUrl(details.getLastBuild().url);

      try {
        //TODO Add progress monitoring
        detail = service.getJobDetails(details.getLastBuild().url, null);
        this.lastBuildAvailable = true;
      } catch (CloudBeesException e) {
        //throw new PartInitException("Failed to load build information from the remote host!", e);
        //TODO Handle!
        e.printStackTrace();
        return;
      }

      System.out.println("Loaded " + detail);

      //setPartName();
      setPartName(details.getJob().displayName + " #" + detail.number);

      //setContentDescription(detail.fullDisplayName);

      String topStr = detail.result != null ? detail.result + " (" + (new Date(detail.timestamp)) + ")" : "";

      textTopSummary.setText(topStr);

      if (form != null) {
        form.setText("Build #" + detail.number + " [" + details.getJob().displayName + "]");
        //TODO Add image for build status! form.setImage(image);
      }

      // Recent Changes      
      loadRecentChanges();

      // Load JUnit Tests
      loadUnitTests();

      System.out.println("here");
    }
  }

  private void loadUnitTests() {
    
    if (detail.actions==null) {
      contentJUnitTests.setText("No Tests");
      return;
    }
    
    for (com.cloudbees.eclipse.core.nectar.api.NectarBuildDetailsResponse.Action action: detail.actions) {
      if ("testReport".equalsIgnoreCase(action.urlName)) {
        String val = "Total: " + action.totalCount + " Failed: " + action.failCount + " Skipped: " + action.skipCount;
        contentJUnitTests.setText(val);
        return;
       }
    }

    contentJUnitTests.setText("No Tests");

  }

  private void loadRecentChanges() {
    StringBuffer changes = new StringBuffer();
    if (detail.changeSet != null && detail.changeSet.items != null) {
      for (ChangeSetItem item : detail.changeSet.items) {
        String authinfo = item.author != null && item.author.fullName != null ? " by " + item.author.fullName : "";
        String line = "rev" + item.rev + ": '" + item.msg + "' " + authinfo + "\n";
        changes.append(line);
      }
    }
    if (changes.length() == 0) {
      changes.append("none");
    }
    contentRecentChanges.setText(changes.toString());
  }

  @Override
  public void setFocus() {
    // Set the focus
    form.setFocus();
  }

  @Override
  public void doSave(IProgressMonitor monitor) {
    // Do the Save operation
  }

  @Override
  public void doSaveAs() {
    // Do the Save As operation
  }

  @Override
  public void init(IEditorSite site, IEditorInput input) throws PartInitException {

    System.out.println("INIT CALLED"); //TODO REMOVEME!

    // Initialize the editor part
    setSite(site);
    setInput(input);

  }

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

  @Override
  public void dispose() {
    form.dispose();
    form = null;
    System.out.println("Form disposed");
    super.dispose();
  }
}
