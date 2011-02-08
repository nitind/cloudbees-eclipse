package com.cloudbees.eclipse.ui.internal.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class JenkinsFinishPage extends AbstractJenkinsPage {

  private static final String DESCR = "Here comes the text describing which cool things you can do now.\nIt's full of possibilities."; // TODO i18n
  private String error;
  private Label labelContentText;

  /**
   * Create the wizard.
   */
  public JenkinsFinishPage() {
    super("finish");
  }

  public void initText(Exception e) {
    error = null;
    if (e != null) {
      // TODO format error nicely, so user can react properly
      error = e.getLocalizedMessage();
      Throwable cause = e.getCause();
      while (cause != null) {
        error += "\n" + cause.getLocalizedMessage();
        cause = cause.getCause();
      }
    }

    if (error == null) {
      setTitle("Congratulations! Jenkins is configured properly");
      setMessage("Specified Jenkins location is working well!");
      //setDescription("Wizard Page description");

    } else {
      setTitle("Failure! Jenkins is not configured properly");
      setMessage("Specified Jenkins location is not working, but you can add it nonetheless!");
      //setDescription("Wizard Page description");
    }

    updateContent();
  }

  /**
   * Create contents of the wizard.
   * @param parent
   */
  public void createControl(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);

    setControl(container);
    GridLayout gl_container = new GridLayout(1, false);
    gl_container.marginWidth = 20;
    gl_container.marginHeight = 0;
    container.setLayout(gl_container);
    
    labelContentText = new Label(container, SWT.WRAP);
    labelContentText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
    updateContent();
  }

  public void updateContent() {
    if (labelContentText == null) {
      return;
    }
    
    String mess = "";

    if (error != null && error.trim().length() > 0) {
      mess += "Error: \n" + error + "\n\n";
    } else {
      mess += DESCR;
    }
    
    labelContentText.setText(mess);
    labelContentText.getParent().layout();
  }
}
