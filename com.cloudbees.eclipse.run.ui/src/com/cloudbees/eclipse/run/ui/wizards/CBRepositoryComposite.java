package com.cloudbees.eclipse.run.ui.wizards;

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

import com.cloudbees.eclipse.core.gc.api.AccountServiceStatusResponse.AccountServices.ForgeService.Repo;

public abstract class CBRepositoryComposite extends Composite {

  private static final String GROUP_LABEL = "Forge repository";
  private static final String FORGE_REPO_CHECK_LABEL = "Add to Forge";
  private static final String ERR_ADD_REPOS = "Please add repositories to your CloudBees DEV@cloud";
  private static final String ERR_REPO_SELECTION = "Repository is not selected.";

  private Repo[] repos;
  private Repo selectedRepo;
  private Button addRepoCheck;
  private Label repoLabel;
  private Combo repoCombo;
  private ComboViewer repoComboViewer;

  public CBRepositoryComposite(Composite parent) {
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

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = CBRepositoryComposite.this.repoComboViewer.getSelection();
        if (selection instanceof StructuredSelection) {
          CBRepositoryComposite.this.selectedRepo = (Repo) ((StructuredSelection) selection).getFirstElement();
        }
        validate();
      }
    });
  }

  public boolean isAddNewRepo() {
    return this.addRepoCheck.getSelection();
  }

  public Repo getSelectedRepo() {
    return this.selectedRepo;
  }

  protected abstract Repo[] getRepos();

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

    updateErrorStatus(null);
  }

  private class MakeForgeRepoSelectionListener implements SelectionListener {

    @Override
    public void widgetSelected(SelectionEvent e) {
      handleEvent();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
      handleEvent();
    }

    private void handleEvent() {
      boolean selected = isAddNewRepo();
      if (selected && CBRepositoryComposite.this.repos == null) {
        CBRepositoryComposite.this.repos = getRepos();
        CBRepositoryComposite.this.repoComboViewer.add(CBRepositoryComposite.this.repos);
      }
      CBRepositoryComposite.this.repoLabel.setEnabled(selected);
      CBRepositoryComposite.this.repoCombo.setEnabled(selected);
      validate();
    }
  }

  private class RepoLabelProvider extends LabelProvider {

    @Override
    public String getText(Object element) {
      if (element instanceof Repo) {
        Repo repo = (Repo) element;
        return repo.url + " [" + repo.type + "]";
      }

      return super.getText(element);
    }

  }

}
