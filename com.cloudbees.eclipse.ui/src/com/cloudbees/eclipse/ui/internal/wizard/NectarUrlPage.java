package com.cloudbees.eclipse.ui.internal.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NectarUrlPage extends AbstractNectarPage {
  private Text textUrl;
  private Text textName;

  /**
   * Create the wizard.
   */
  public NectarUrlPage() {
    super("url");
    setMessage("Please provide a URL and label for your connection.");
    setTitle("New Nectar location");
    setDescription("New Nectar location");
    setPageComplete(false);
  }

  /**
   * Create contents of the wizard.
   * 
   * @param parent
   */
  public void createControl(Composite parent) {
    Composite comp = new Composite(parent, SWT.NULL);

    setControl(comp);
    GridLayout gl_comp = new GridLayout(2, false);
    gl_comp.marginWidth = 20;
    gl_comp.marginHeight = 40;
    comp.setLayout(gl_comp);

    Label labelUrl = new Label(comp, SWT.NONE);
    labelUrl.setToolTipText("Nectar location URL");
    labelUrl.setText("Nectar &URL:");

    textUrl = new Text(comp, SWT.BORDER);
    textUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    textUrl.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        validate();
      }
    });

    Label lblName = new Label(comp, SWT.NONE);
    lblName.setText("Local display &label:");

    textName = new Text(comp, SWT.BORDER);
    textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    textName.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
          validate();
      }
    });

  }

  private void validate() {
    if (textUrl.getText().length() == 0) {
      setErrorMessage("Url is empty!"); //TODO i18n
      setPageComplete(false);
    }

    if (textName.getText().length() == 0) {
      setErrorMessage("Label is empty!");//TODO I18n
      setPageComplete(false);
    }

    ni.label = textName.getText();
    ni.url = textUrl.getText();

    setErrorMessage(null);
    setPageComplete(true);
  }

  @Override
  public IWizardPage getNextPage() {
    return super.getNextPage();
  }


}
