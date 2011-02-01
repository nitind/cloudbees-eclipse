package com.cloudbees.eclipse.ui.internal.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class NectarFinishPage extends AbstractNectarPage {

  /**
   * Create the wizard.
   */
  public NectarFinishPage() {
    super("finish");
    setTitle("New Nectar location added");
    setMessage("Congratulations! Nectar location has been successfully added!");
    //setDescription("Wizard Page description");
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
    
    Label labelSuccessText = new Label(container, SWT.WRAP);
    labelSuccessText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
    labelSuccessText.setText("Here comes the text describing which cool things you can do now.\nIt's full of possibilities.");
  }
}
