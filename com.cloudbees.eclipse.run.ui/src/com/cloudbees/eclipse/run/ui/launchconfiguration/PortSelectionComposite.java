package com.cloudbees.eclipse.run.ui.launchconfiguration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

public abstract class PortSelectionComposite extends Composite {

  private static final String DEFAULT_PORT = "8335";
  private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());
  private final Text text;
  private final Button btnUseDefaultPort;
  private final Label lblPort;

  /**
   * Create the composite.
   * 
   * @param parent
   * @param style
   */
  public PortSelectionComposite(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout(SWT.HORIZONTAL));

    Group grpSelectPort = new Group(this, SWT.NONE);
    grpSelectPort.setText("HTTP Port");
    GridLayout gl_grpSelectPort = new GridLayout(2, false);
    grpSelectPort.setLayout(gl_grpSelectPort);

    this.btnUseDefaultPort = new Button(grpSelectPort, SWT.CHECK);
    this.btnUseDefaultPort.setSelection(true);
    this.btnUseDefaultPort.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    this.btnUseDefaultPort.setText("Use default port (" + DEFAULT_PORT + ")");
    this.btnUseDefaultPort.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        handleChange();
      }

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
      }
    });

    this.lblPort = new Label(grpSelectPort, SWT.NONE);
    this.lblPort.setEnabled(false);
    GridData gd_lblPort = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
    gd_lblPort.horizontalIndent = 22;
    this.lblPort.setLayoutData(gd_lblPort);
    this.lblPort.setText("Port");

    this.text = new Text(grpSelectPort, SWT.BORDER);
    this.text.setEnabled(false);
    this.text.setText(DEFAULT_PORT);
    this.text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
    this.text.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        handleChange();
      }
    });
    addDisposeListener(new DisposeListener() {
      @Override
      public void widgetDisposed(DisposeEvent e) {
        PortSelectionComposite.this.toolkit.dispose();
      }
    });
    this.toolkit.adapt(this);
    this.toolkit.paintBordersFor(this);
    this.btnUseDefaultPort.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        PortSelectionComposite.this.lblPort.setEnabled(!PortSelectionComposite.this.btnUseDefaultPort.getSelection());
        PortSelectionComposite.this.text.setEnabled(!PortSelectionComposite.this.btnUseDefaultPort.getSelection());
      }
    });

  }

  public String getPort() {
    if (this.btnUseDefaultPort.getSelection()) {
      return DEFAULT_PORT;
    } else {
      return this.text.getText();
    }
  }

  public void setPort(String port) {
    if (port == null || "".equals(port) || DEFAULT_PORT.equals(port)) {
      this.btnUseDefaultPort.setSelection(true);
    } else {
      this.btnUseDefaultPort.setSelection(false);
      this.text.setText(port);
    }
    this.lblPort.setEnabled(!PortSelectionComposite.this.btnUseDefaultPort.getSelection());
    PortSelectionComposite.this.text.setEnabled(!PortSelectionComposite.this.btnUseDefaultPort.getSelection());
  }

  public abstract void handleChange();

}
