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
  public JenkinsUrlPage() {
    super("url");
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

  public void setJenkinsInstance(JenkinsInstance ni) {
    super.setJenkinsInstance(ni);
    init();
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

    Label lblUrl = new Label(comp, SWT.NONE);
    lblUrl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblUrl.setToolTipText("Jenkins location URL");
    lblUrl.setText("Jenkins &URL:");

    textUrl = new Text(comp, SWT.BORDER);
    textUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    Label lblName = new Label(comp, SWT.NONE);
    lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblName.setText("Local display &label:");

    textLabel = new Text(comp, SWT.BORDER);
    textLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    new Label(comp, SWT.NONE);
    new Label(comp, SWT.NONE);

    chkAuthenticate = new Button(comp, SWT.CHECK);
    chkAuthenticate.setText("&Authenticate");

    new Label(comp, SWT.NONE);

    final Label lblUsername = new Label(comp, SWT.NONE);
    lblUsername.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblUsername.setText("&Username:");

    textUsername = new Text(comp, SWT.BORDER);
    textUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    final Label lblPassword = new Label(comp, SWT.NONE);
    lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    lblPassword.setText("&Password:");

    textPassword = new Text(comp, SWT.BORDER | SWT.PASSWORD);
    textPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

    ModifyListener modifyListener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        validate();
      }
    };
    textUrl.addModifyListener(modifyListener);
    textLabel.addModifyListener(modifyListener);
    textUsername.addModifyListener(modifyListener);
    textPassword.addModifyListener(modifyListener);

    SelectionAdapter selectionListener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        validate();
        boolean auth = chkAuthenticate.getSelection();
        textUsername.setEnabled(auth);
        textPassword.setEnabled(auth);
        lblUsername.setEnabled(auth);
        lblPassword.setEnabled(auth);
        if (!auth) {
          textUsername.setText("");
          textPassword.setText("");
        }
        validate();
      }
    };
    chkAuthenticate.addSelectionListener(selectionListener);

    setText(textUrl, instance.url);
    setText(textLabel, instance.label);
    setText(textUsername, instance.username);
    setText(textPassword, instance.password);
    chkAuthenticate.setSelection(instance.authenticate);
    selectionListener.widgetSelected(null);
  }

  private void setText(Text control, String text) {
    control.setText(text != null ? text : "");
  }

  private void validate() {
    if (textUrl == null) {
      return; // not yet
    }

    if (textUrl.getText().length() == 0) {
      setErrorMessage("Url is empty!"); // TODO i18n
      setPageComplete(false);
      return;
    }

    if (textLabel.getText().length() == 0) {
      setErrorMessage("Label is empty!");// TODO i18n
      setPageComplete(false);
      return;
    }

    if (chkAuthenticate.getSelection()) {
      if (textUsername.getText().trim().length() == 0) {
        setErrorMessage("Username is empty!"); // TODO i18n
        setPageComplete(false);
        return;
      }

      if (textPassword.getText().trim().length() == 0) {
        setErrorMessage("Password is empty!");// TODO i18n
        setPageComplete(false);
        return;
      }
    }

    instance.url = textUrl.getText().trim();
    instance.label = textLabel.getText().trim();
    instance.username = textUsername.getText().trim();
    instance.password = textPassword.getText().trim();
    instance.authenticate = chkAuthenticate.getSelection();

    setErrorMessage(null);
    setPageComplete(true);
  }

  @Override
  public IWizardPage getNextPage() {
    return super.getNextPage();
  }

}
