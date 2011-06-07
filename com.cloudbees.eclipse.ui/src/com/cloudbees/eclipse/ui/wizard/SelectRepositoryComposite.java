package com.cloudbees.eclipse.ui.wizard;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.cloudbees.eclipse.core.forge.api.ForgeInstance;

public abstract class SelectRepositoryComposite extends Composite {

  private static final String GROUP_LABEL = "SVN Forge repository";
  private static final String FORGE_REPO_CHECK_LABEL = "Host at Forge SVN";
  private static final String ERR_ADD_REPOS = "Please add SVN repositories to your CloudBees DEV@cloud";
  private static final String ERR_REPO_SELECTION = "SVN repository is not selected.";
  private static final String ERR_REPO_NOT_SYNCED = "SVN repository not locally configured!";

  private ForgeInstance[] repos;
  private ForgeInstance selectedRepo;
  private Button addRepoCheck;
  private Label repoLabel;
  private Combo repoCombo;
  private ComboViewer repoComboViewer;

  public SelectRepositoryComposite(final Composite parent) {
    super(parent, SWT.NONE);
    init();
  }

  private void init() {

    FillLayout layout = new FillLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    layout.spacing = 0;

    setLayout(layout);

    Group group = new Group(this, SWT.NONE);
    group.setText(GROUP_LABEL);
    group.setLayout(new GridLayout(2, false));

    GridData data = new GridData();
    data.horizontalSpan = 2;
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.LEFT;

    this.addRepoCheck = new Button(group, SWT.CHECK);
    this.addRepoCheck.setText(FORGE_REPO_CHECK_LABEL);
    this.addRepoCheck.setSelection(false);
    this.addRepoCheck.setLayoutData(data);
    this.addRepoCheck.addSelectionListener(new MakeForgeRepoSelectionListener());

    data = new GridData();
    data.verticalAlignment = SWT.CENTER;

    this.repoLabel = new Label(group, SWT.NULL);
    this.repoLabel.setLayoutData(data);
    this.repoLabel.setText("Repository:");
    this.repoLabel.setEnabled(false);

    data = new GridData();
    data.grabExcessHorizontalSpace = true;
    data.horizontalAlignment = SWT.FILL;

    this.repoCombo = new Combo(group, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
    this.repoCombo.setLayoutData(data);
    this.repoCombo.setEnabled(false);
    this.repoComboViewer = new ComboViewer(this.repoCombo);
    this.repoComboViewer.setLabelProvider(new RepoLabelProvider());
    this.repoComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      public void selectionChanged(final SelectionChangedEvent event) {
        ISelection selection = SelectRepositoryComposite.this.repoComboViewer.getSelection();
        if (selection instanceof StructuredSelection) {
          SelectRepositoryComposite.this.selectedRepo = (ForgeInstance) ((StructuredSelection) selection)
              .getFirstElement();
        }
        validate();
      }
    });
  }

  public boolean isAddNewRepo() {
    return this.addRepoCheck.getSelection();
  }

  public ForgeInstance getSelectedRepo() {
    return this.selectedRepo;
  }

  public void addRepoCheckListener(final SelectionListener listener) {
    if (listener != null && this.addRepoCheck != null) {
      this.addRepoCheck.addSelectionListener(listener);
    }
  }

  protected abstract ForgeInstance[] getRepos();

  protected abstract void updateErrorStatus(String errorMsg);

  private void validate() {
    if (!isAddNewRepo()) {
      updateErrorStatus(null);
      return;
    }

    if (this.repos == null || this.repos.length == 0) {
      updateErrorStatus(ERR_ADD_REPOS);
      return;
    }

    if (getSelectedRepo() == null) {
      updateErrorStatus(ERR_REPO_SELECTION);
      return;
    }

    if (getSelectedRepo().status!=ForgeInstance.STATUS.SYNCED) {
      updateErrorStatus(ERR_REPO_NOT_SYNCED);
      return;      
    }
    
    updateErrorStatus(null);
  }

  private class MakeForgeRepoSelectionListener implements SelectionListener {

    public void widgetSelected(final SelectionEvent e) {
      handleEvent();
    }

    public void widgetDefaultSelected(final SelectionEvent e) {
      handleEvent();
    }

    private void handleEvent() {
      boolean selected = isAddNewRepo();
      if (selected && SelectRepositoryComposite.this.repos == null) {
        SelectRepositoryComposite.this.repos = getRepos();
        SelectRepositoryComposite.this.repoComboViewer.add(SelectRepositoryComposite.this.repos);
      }
      SelectRepositoryComposite.this.repoLabel.setEnabled(selected);
      SelectRepositoryComposite.this.repoCombo.setEnabled(selected);
      validate();
    }
  }

  private class RepoLabelProvider extends LabelProvider {

    @Override
    public String getText(final Object element) {
      if (element instanceof ForgeInstance) {
        ForgeInstance repo = (ForgeInstance) element;
        return repo.url + " [" + repo.type.toString().toLowerCase() + "]";
      }

      return super.getText(element);
    }

  }

}
