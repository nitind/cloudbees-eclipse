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
import org.eclipse.ui.forms.widgets.FormText;
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
  private FormText textChanges;
  private FormText textTests;
  private FormText textSummary;
  private FormText textHistory;
  private Label textTopSummary;

  public BuildPart() {
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

    textSummary = formToolkit.createFormText(sectSummary, false);
    formToolkit.paintBordersFor(textSummary);
    sectSummary.setClient(textSummary);

    Section sectBuildHistory = formToolkit.createSection(composite, Section.TITLE_BAR);
    formToolkit.paintBordersFor(sectBuildHistory);
    sectBuildHistory.setText("Build History");

    textHistory = formToolkit.createFormText(sectBuildHistory, false);
    formToolkit.paintBordersFor(textHistory);
    sectBuildHistory.setClient(textHistory);

    Section sectRecentChanges = formToolkit.createSection(composite, Section.DESCRIPTION | Section.TITLE_BAR);
    formToolkit.paintBordersFor(sectRecentChanges);
    sectRecentChanges.setText("Recent Changes");
    sectRecentChanges.setExpanded(false);

    textChanges = formToolkit.createFormText(sectRecentChanges, false);
    sectRecentChanges.setClient(textChanges);
    formToolkit.paintBordersFor(textChanges);

    Section sectTests = formToolkit.createSection(composite, Section.TITLE_BAR);
    formToolkit.paintBordersFor(sectTests);
    sectTests.setText("JUnit Tests");

    textTests = formToolkit.createFormText(sectTests, false);
    sectTests.setClient(textTests);
    formToolkit.paintBordersFor(textTests);

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


    form.getToolBarManager().add(haction);
    form.getToolBarManager().add(new Separator());

    loadData();

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
      setPartName(details.getJob().displayName);

      //setContentDescription(detail.fullDisplayName);

      String topStr = detail.result != null ? detail.result + " (" + (new Date(detail.timestamp)) + ")" : "";
      textTopSummary.setText(topStr);

      if (form != null) {
        form.setText("Build #" + detail.number + " [" + details.getJob().displayName + "]");
        //TODO Add image for build status! form.setImage(image);
      }

      // Recent Changes
      StringBuffer changes = new StringBuffer();
      changes.append("HEI");
      if (detail.changeSet != null && detail.changeSet.items != null) {
        for (ChangeSetItem item : detail.changeSet.items) {
          String line = "#" + item.rev + " " + item.msg + "<br/>";
          changes.append(line);
        }
      }
      textChanges.setText(changes.toString(), false, false);
      System.out.println("CHANGED:" + changes.toString());

    }
  }

  @Override
  public void setFocus() {
    // Set the focus
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
