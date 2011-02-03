package com.cloudbees.eclipse.ui.views.jobdetails;

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

public class JobDetailsPart extends EditorPart {

  public static final String ID = "com.cloudbees.eclipse.ui.views.jobdetails.JobDetailsPart"; //$NON-NLS-1$
  private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());

  public JobDetailsPart() {
  }

  /**
   * Create contents of the editor part.
   * @param parent
   */
  @Override
  public void createPartControl(Composite parent) {

    ScrolledForm scrldfrmNewScrolledform = formToolkit.createScrolledForm(parent);
    formToolkit.decorateFormHeading(scrldfrmNewScrolledform.getForm());
    formToolkit.paintBordersFor(scrldfrmNewScrolledform);
    scrldfrmNewScrolledform.setText("New ScrolledForm");
    scrldfrmNewScrolledform.getBody().setLayout(new ColumnLayout());

    Section sctnNewSection = formToolkit.createSection(scrldfrmNewScrolledform.getBody(), Section.TWISTIE
        | Section.TITLE_BAR);
    formToolkit.paintBordersFor(sctnNewSection);
    sctnNewSection.setText("New Section");

    Label label = formToolkit.createSeparator(scrldfrmNewScrolledform.getBody(), SWT.NONE);
    ColumnLayoutData cld_label = new ColumnLayoutData();
    cld_label.heightHint = 428;
    label.setLayoutData(cld_label);

    Section sctnNewSection_1 = formToolkit.createSection(scrldfrmNewScrolledform.getBody(), Section.TWISTIE
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
  }

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public boolean isSaveAsAllowed() {
    return false;
  }

}
