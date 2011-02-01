package com.cloudbees.eclipse.ui.views.jobdetails;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

public class JobDetailsView extends ViewPart {

  public static final String ID = "com.cloudbees.eclipse.ui.views.jobdetails.JobDetailsView"; //$NON-NLS-1$
  private final FormToolkit toolkit = new FormToolkit(Display.getCurrent());

  public JobDetailsView() {
  }

  /**
   * Create contents of the view part.
   * @param parent
   */
  @Override
  public void createPartControl(Composite parent) {
    //Composite container = toolkit.createComposite(parent, SWT.NONE);
    //toolkit.paintBordersFor(container);

    ScrolledForm form = toolkit.createScrolledForm(parent);

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

    createActions();
    initializeToolBar();
    initializeMenu();
  }

  public void dispose() {
    toolkit.dispose();
    super.dispose();
  }

  /**
   * Create the actions.
   */
  private void createActions() {
    // Create the actions
  }

  /**
   * Initialize the toolbar.
   */
  private void initializeToolBar() {
    IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
  }

  /**
   * Initialize the menu.
   */
  private void initializeMenu() {
    IMenuManager manager = getViewSite().getActionBars().getMenuManager();
  }

  @Override
  public void setFocus() {
    // Set the focus
  }

}
