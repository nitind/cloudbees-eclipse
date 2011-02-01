package com.cloudbees.eclipse.ui.internal.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NectarAuthenticationPage extends AbstractNectarPage {
  private Text textUsername;
  private Text textPassword;

  /**
   * Create the wizard.
   */
  public NectarAuthenticationPage() {
    super("auth");
    setErrorMessage("This Nectar instance requires authentication. Please provide the credentials.");
    setMessage("This Nectar instance requires authentication. Please provide the credentials.");
    setTitle("Authentication required!");
    setDescription("Wizard Page description");
  }

  /**
   * Create contents of the wizard.
   * @param parent
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);

    setControl(container);
    GridLayout gl_container = new GridLayout(2, false);
    gl_container.marginWidth = 20;
    gl_container.marginHeight = 40;
    container.setLayout(gl_container);
    
    Label LabelNectarURL = new Label(container, SWT.NONE);
    LabelNectarURL.setText("Nectar URL:");
    
    Label labelURLValue = new Label(container, SWT.NONE);
    labelURLValue.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
    
    Label labelUsername = new Label(container, SWT.NONE);
    labelUsername.setText("&Username:");
    
    textUsername = new Text(container, SWT.BORDER);
    textUsername.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    
    Label labelPassword = new Label(container, SWT.NONE);
    labelPassword.setText("&Password:");
    
    textPassword = new Text(container, SWT.BORDER | SWT.PASSWORD);
    textPassword.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

    labelURLValue.setText(ni != null && ni.url != null ? ni.url : "");

  }

}
