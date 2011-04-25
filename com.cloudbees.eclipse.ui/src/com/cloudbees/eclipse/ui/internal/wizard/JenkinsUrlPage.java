package com.cloudbees.eclipse.ui.internal.wizard;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.cloudbees.eclipse.core.domain.JenkinsInstance;

public class JenkinsUrlPage extends AbstractJenkinsPage {

  private Text textUrl;
  private Text textLabel;
  private Text textUsername;
  private Text textPassword;
  private Button chkAuthenticate;

  /**
   * Create the wizard.
   */
  public JenkinsUrlPage(final JenkinsInstance ni) {
    super("url");
    setJenkinsInstance(ni);
    setMessage("Please provide a URL and label for your connection.");
    init();
  }

  private void init() {
    if (isEditMode()) {
      setTitle("Edit Jenkins location");
      setDescription("Edit Jenkins location");
      validate();
    } else {
      setTitle("New Jenkins location");
      setDescription("New Jenkins location");
      setPageComplete(false);
    }
  }

  /**
   * Create contents of the wizard.
   *
   * @param parent
   */
  public void createControl(final Composite parent) {
    Composite comp = new Composite(parent, SWT.NULL);

    setControl(comp);
    GridLayout gl_comp = new GridLayout(2, false);
    gl_comp.marginWidth = 20;
    gl_comp.marginHeight = 40;
    comp.setLayout(gl_comp);

    Label lblName = new Label(comp, SWT.NONE);
    lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblName.setText("Local display &label:");

    this.textLabel = new Text(comp, SWT.BORDER);
    this.textLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Label lblUrl = new Label(comp, SWT.NONE);
    lblUrl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblUrl.setToolTipText("Jenkins location URL");
    lblUrl.setText("Jenkins &URL:");

    this.textUrl = new Text(comp, SWT.BORDER);
    this.textUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    new Label(comp, SWT.NONE);
    new Label(comp, SWT.NONE);

    this.chkAuthenticate = new Button(comp, SWT.CHECK);
    this.chkAuthenticate.setText("&Authenticate");

    new Label(comp, SWT.NONE);

    final Label lblUsername = new Label(comp, SWT.NONE);
    lblUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblUsername.setText("&Username:");

    this.textUsername = new Text(comp, SWT.BORDER);
    this.textUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    final Label lblPassword = new Label(comp, SWT.NONE);
    lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblPassword.setText("&Password:");

    this.textPassword = new Text(comp, SWT.BORDER | SWT.PASSWORD);
    this.textPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    ModifyListener modifyListener = new ModifyListener() {
      public void modifyText(final ModifyEvent e) {
        validate();
      }
    };

    SelectionAdapter selectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(final SelectionEvent e) {
        validate();
        boolean auth = JenkinsUrlPage.this.chkAuthenticate.getSelection();
        JenkinsUrlPage.this.textUsername.setEnabled(auth);
        JenkinsUrlPage.this.textPassword.setEnabled(auth);
        lblUsername.setEnabled(auth);
        lblPassword.setEnabled(auth);
        if (!auth) {
          JenkinsUrlPage.this.textUsername.setText("");
          JenkinsUrlPage.this.textPassword.setText("");
        }
        validate();
      }
    };
    this.chkAuthenticate.addSelectionListener(selectionListener);

    setText(this.textUrl, this.instance.url);
    setText(this.textLabel, this.instance.label);
    setText(this.textUsername, this.instance.username);
    setText(this.textPassword, this.instance.password);
    this.chkAuthenticate.setSelection(this.instance.authenticate);

    selectionListener.widgetSelected(null);

    this.textUrl.addModifyListener(modifyListener);
    this.textLabel.addModifyListener(modifyListener);
    this.textUsername.addModifyListener(modifyListener);
    this.textPassword.addModifyListener(modifyListener);
  }

  private void setText(final Text control, final String text) {
    control.setText(text != null ? text : "");
  }

  private void validate() {
    if (this.textUrl == null) {
      return; // not yet
    }

    if (this.textUrl.getText().length() == 0) {
      setErrorMessage("Url is empty!"); // TODO i18n
      setPageComplete(false);
      return;
    }

    if (this.textLabel.getText().length() == 0) {
      setErrorMessage("Label is empty!");// TODO i18n
      setPageComplete(false);
      return;
    }

    if (this.chkAuthenticate.getSelection()) {
      if (this.textUsername.getText().trim().length() == 0) {
        setErrorMessage("Username is empty!"); // TODO i18n
        setPageComplete(false);
        return;
      }

      if (this.textPassword.getText().trim().length() == 0) {
        setErrorMessage("Password is empty!");// TODO i18n
        setPageComplete(false);
        return;
      }
    }

    this.instance.url = this.textUrl.getText().trim();
    this.instance.label = this.textLabel.getText().trim();
    this.instance.username = this.textUsername.getText().trim();
    this.instance.password = this.textPassword.getText().trim();
    this.instance.authenticate = this.chkAuthenticate.getSelection();

    setErrorMessage(null);
    setPageComplete(true);
  }

  @Override
  public IWizardPage getNextPage() {
    return super.getNextPage();
  }

}
