package com.cloudbees.eclipse.ui.views.jobdetails;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class JobDetailsForm extends FormPage {

  public static final String ID = "com.cloudbees.eclipse.ui.views.jobdetails.JobDetailsForm";

  private ScrolledForm form;
  private FormToolkit toolkit;

  public JobDetailsForm() {
    super(ID, null);
  }

  public JobDetailsForm(String id, String title) {
    super(id, title);
  }

  /**
   * Create the form page.
   * 
   * @param id
   * @param editor
   * @param id
   * @param title
   * @param title
   * @wbp.parser.constructor
   * @wbp.eval.method.parameter id "Some id"
   * @wbp.eval.method.parameter title "Some title"
   */
  public JobDetailsForm(FormEditor editor, String id, String title) {
    super(editor, id, title);
    setContentDescription("Information about your build number blabla");
    setPartName("Build #12413");
  }

  public void createFormContent(IManagedForm managedForm) {
    FormToolkit toolkit = managedForm.getToolkit();
    ScrolledForm form = managedForm.getForm();

    form.setText("Build for #123123");
    Composite body = form.getBody();
    toolkit.decorateFormHeading(form.getForm());
    toolkit.paintBordersFor(body);

    Label lblNewLabel = toolkit.createLabel(body, "New Label", SWT.NONE);
    lblNewLabel.setBounds(10, 10, 58, 13);

    ExpandableComposite xpndblcmpstAsdfasdf = toolkit.createExpandableComposite(body, ExpandableComposite.TWISTIE);

    xpndblcmpstAsdfasdf.setBounds(10, 29, 190, 13);
    toolkit.paintBordersFor(xpndblcmpstAsdfasdf);
    xpndblcmpstAsdfasdf.setText("asdfasdf");
    xpndblcmpstAsdfasdf.setExpanded(true);

    Label lblEee = toolkit.createLabel(xpndblcmpstAsdfasdf, "eee", SWT.NONE);
    xpndblcmpstAsdfasdf.setClient(lblEee);

    Button btnNewButton = toolkit.createButton(body, "New Button", SWT.NONE);
    btnNewButton.setBounds(10, 48, 77, 25);
  }

  @Override
  public void setFocus() {
    form.setFocus();
  }

  public void dispose() {
    toolkit.dispose();
    super.dispose();
  }
}
