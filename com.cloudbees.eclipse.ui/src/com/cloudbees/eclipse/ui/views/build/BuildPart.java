package com.cloudbees.eclipse.ui.views.build;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.core.NectarService;
import com.cloudbees.eclipse.core.nectar.api.NectarBuildDetailsResponse;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class BuildPart extends EditorPart {

  public static final String ID = "com.cloudbees.eclipse.ui.views.build.BuildPart"; //$NON-NLS-1$
  private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

  private NectarBuildDetailsResponse detail;

  private boolean lastBuildAvailable = false;
  private ScrolledForm form;

  public BuildPart() {
  }

  /**
   * Create contents of the editor part.
   * 
   * @param parent
   */
  @Override
  public void createPartControl(Composite parent) {

    form = formToolkit.createScrolledForm(parent);
    formToolkit.decorateFormHeading(form.getForm());
    formToolkit.paintBordersFor(form);
    form.setText("n/a");
    form.getBody().setLayout(new ColumnLayout());

    Section sctnNewSection = formToolkit.createSection(form.getBody(), Section.TWISTIE
        | Section.TITLE_BAR);
    formToolkit.paintBordersFor(sctnNewSection);
    sctnNewSection.setText("New Section");

    Label label = formToolkit.createSeparator(form.getBody(), SWT.NONE);
    ColumnLayoutData cld_label = new ColumnLayoutData();
    cld_label.heightHint = 428;
    label.setLayoutData(cld_label);

    Section sctnNewSection_1 = formToolkit.createSection(form.getBody(), Section.TWISTIE
        | Section.TITLE_BAR);
    formToolkit.paintBordersFor(sctnNewSection_1);
    sctnNewSection_1.setText("New Section");

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
    // Initialize the editor part
    setSite(site);
    setInput(input);
    BuildEditorInput details = (BuildEditorInput) input;

    this.lastBuildAvailable = false;

    if (details == null || details.getLastBuild() == null || details.getLastBuild().url == null) {
      // No last build available
    } else {

      IProgressMonitor monitor = null;

      NectarService service = CloudBeesUIPlugin.getDefault().getNectarServiceForUrl(details.getLastBuild().url);

      try {
        detail = service.getJobDetails(details.getLastBuild().url, monitor);
        this.lastBuildAvailable = true;
      } catch (CloudBeesException e) {
        throw new PartInitException("Failed to load build information from the remote host!", e);
      }

      System.out.println("Loaded " + detail);

      //setPartName();
      setContentDescription("Build details for " + detail.fullDisplayName);

      if (form != null) {
        form.setText("Build #" + detail.number);
      }

    }

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
    super.dispose();
  }

}
