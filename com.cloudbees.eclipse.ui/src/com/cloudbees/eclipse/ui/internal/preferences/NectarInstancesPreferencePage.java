package com.cloudbees.eclipse.ui.internal.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

/**
 * CloudBees Nectar instances configuration
 * 
 * @author ahtik
 */

public class NectarInstancesPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  public NectarInstancesPreferencePage() {
    super(GRID);
    setPreferenceStore(CloudBeesUIPlugin.getDefault().getPreferenceStore());
    setDescription("TODO/Work in progress!");

  }

  public void createFieldEditors() {

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
