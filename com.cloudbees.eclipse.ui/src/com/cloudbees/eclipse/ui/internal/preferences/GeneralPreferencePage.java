package com.cloudbees.eclipse.ui.internal.preferences;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.cloudbees.eclipse.core.CloudBeesCorePlugin;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.Messages;
import com.cloudbees.eclipse.ui.PreferenceConstants;

/**
 * CloudBees account info settings
 * 
 * @author ahtik
 */

public class GeneralPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  public GeneralPreferencePage() {
    super(GRID);
    setPreferenceStore(CloudBeesUIPlugin.getDefault().getPreferenceStore());
    setDescription(Messages.pref_description);

  }

  public void createFieldEditors() {

    getFieldEditorParent().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    createCompositeLogin();

    new Label(getFieldEditorParent(), SWT.NONE);

    createCompositeServices();

  }

  private void createCompositeServices() {
    Group group = new Group(getFieldEditorParent(), SWT.SHADOW_ETCHED_IN);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new GridLayout(1, false));

    Composite groupInnerComp = new Composite(group, SWT.NONE);
    groupInnerComp.setLayout(new GridLayout(2, false));
    groupInnerComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    group.setText(Messages.pref_group_devAtCloud);

    addField(new BooleanFieldEditor(PreferenceConstants.P_ENABLE_HAAS, Messages.pref_enable_haas, groupInnerComp));
    addField(new BooleanFieldEditor(PreferenceConstants.P_ENABLE_FORGE, Messages.pref_enable_forge, groupInnerComp));

    createAttachNectarLink(groupInnerComp);
  }

  private void createCompositeLogin() {
    Group group = new Group(getFieldEditorParent(), SWT.SHADOW_ETCHED_IN);
    group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    group.setLayout(new GridLayout(1, false));

    Composite groupInnerComp = new Composite(group, SWT.NONE);
    groupInnerComp.setLayout(new GridLayout(2, false));
    groupInnerComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    group.setText(Messages.pref_group_login);

    final StringFieldEditor fieldEmail = new StringFieldEditor(PreferenceConstants.P_EMAIL, Messages.pref_email, 30,
        groupInnerComp);
    addField(fieldEmail);

    final StringFieldEditor fieldPassword = new StringFieldEditor(PreferenceConstants.P_PASSWORD,
        Messages.pref_password, 30, groupInnerComp) {

      protected void doLoad() {
        try {
          if (getTextControl() != null) {
            String value = SecurePreferencesFactory.getDefault().get(PreferenceConstants.P_PASSWORD, "");
            getTextControl().setText(value);
            oldValue = value;
          }
        } catch (StorageException e) {
          // Ignore StorageException, very likely just
          // "No password provided."
        }
      }

      protected void doStore() {
        try {
          SecurePreferencesFactory.getDefault().put(PreferenceConstants.P_PASSWORD, getTextControl().getText(), true);
        } catch (StorageException e) {
          e.printStackTrace();
          CloudBeesUIPlugin.showError(
              "Saving password failed!\nPossible cause: Eclipse security master password is not set.", e);
        }
      }

      protected void doFillIntoGrid(Composite parent, int numColumns) {
        super.doFillIntoGrid(parent, numColumns);
        getTextControl().setEchoChar('*');
      }
    };
    addField(fieldPassword);

    Button b = new Button(groupInnerComp, SWT.PUSH);
    b.setText(Messages.pref_validate_login);
    b.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {

        try {
          final String email = fieldEmail.getStringValue();
          final String password = fieldPassword.getStringValue();

          // no need to fork at this point
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(false, true, new IRunnableWithProgress() {

            public void run(IProgressMonitor monitor) {
              try {
                boolean loginValid = CloudBeesCorePlugin.getDefault().getGrandCentralService()
                    .remoteValidateUser(email, password, monitor);
                if (loginValid) {
                  MessageDialog.openInformation(CloudBeesUIPlugin.getDefault().getWorkbench().getDisplay()
                      .getActiveShell(), "Validation result", "Validation successful!");
                } else {
                  MessageDialog.openError(CloudBeesUIPlugin.getDefault().getWorkbench().getDisplay().getActiveShell(),
                      "Validation result", "Validation was not successful!\nWrong email or password?");
                }

              } catch (CloudBeesException e1) {
                e1.printStackTrace();
                // TODO Log properly
                CloudBeesUIPlugin.showError("Failed to validate your account.", e1);
              }
            }
          });
        } catch (InvocationTargetException e1) {
          e1.printStackTrace();
          // TODO Log properly
          CloudBeesUIPlugin.showError("Failed to validate your account.", e1.getTargetException());
        } catch (InterruptedException e1) {
          e1.printStackTrace();
          // TODO Log properly
          CloudBeesUIPlugin.showError("Failed to validate your account.", e1);
        }

      }
    });

    createSignUpLink(groupInnerComp);
  }

  private void createAttachNectarLink(Composite parent) {
    final Link link = new Link(parent, SWT.NONE);
    link.setText(Messages.pref_attach_nectar);
    link.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        PreferencesUtil.createPreferenceDialogOn(link.getShell(),
            "com.cloudbees.eclipse.ui.preferences.NectarInstancesPreferencePage", null, null);
      }
    });
    String linktooltip = Messages.pref_attach_nectar_tooltip;
    link.setToolTipText(linktooltip);
  }

  private void createSignUpLink(Composite parent) {
    final Link link = new Link(parent, SWT.NONE);
    link.setText(Messages.pref_signup);
    link.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        openSignupPage();
      }
    });
    String linktooltip = Messages.pref_signup_tooltip;
    link.setToolTipText(linktooltip);
  }

  private void openSignupPage() {
    IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
    try {
      IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.LOCATION_BAR
          | IWorkbenchBrowserSupport.NAVIGATION_BAR, null, null, null);
      browser.openURL(new URL("https://grandcentral.cloudbees.com/account/signup"));
    } catch (PartInitException e) {
      // TODO Log!
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Log!
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
  }

}
