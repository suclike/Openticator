package com.arturogutierrez.openticator.domain.account.list.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import butterknife.Bind;
import com.arturogutierrez.openticator.R;
import com.arturogutierrez.openticator.domain.account.list.AccountListPresenter;
import com.arturogutierrez.openticator.domain.account.list.AccountListView;
import com.arturogutierrez.openticator.domain.account.list.adapter.AccountViewHolder;
import com.arturogutierrez.openticator.domain.account.list.adapter.AccountsAdapter;
import com.arturogutierrez.openticator.domain.account.list.di.AccountListComponent;
import com.arturogutierrez.openticator.domain.account.model.AccountPasscode;
import com.arturogutierrez.openticator.view.fragment.BaseFragment;
import java.util.List;
import javax.inject.Inject;

public class AccountListFragment extends BaseFragment implements AccountListView {

  @Inject
  AccountListPresenter presenter;

  @Bind(R.id.rv_accounts)
  RecyclerView rvAccounts;

  @Bind(R.id.tv_empty_view)
  TextView tvEmptyView;
  ActionBar actionBar;

  private AccountsAdapter accountsAdapter;

  public AccountListFragment() {
    super();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    initialize();
  }

  @Override
  public void onResume() {
    super.onResume();
    presenter.resume();
  }

  @Override
  public void onPause() {
    super.onPause();
    presenter.pause();
    stopCounters();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    presenter.destroy();
  }

  @Override
  protected int getLayoutResource() {
    return R.layout.fragment_account_list;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.account_list, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_export:
        presenter.createBackup();
        break;
      case R.id.action_import:
        break;
    }

    return false;
  }

  @Override
  protected void configureUI() {
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

    rvAccounts.setLayoutManager(linearLayoutManager);
  }

  private void initialize() {
    initializeInjector();
    actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    presenter.setView(this);
    setHasOptionsMenu(true);
  }

  private void initializeInjector() {
    getComponent().inject(this);
  }

  private AccountListComponent getComponent() {
    return getComponent(AccountListComponent.class);
  }

  @Override
  public void viewNoItems() {
    showEmptyView();
  }

  @Override
  public void renderAccounts(List<AccountPasscode> accounts) {
    showAccountList(accounts);
  }

  @Override
  public void startEditMode() {
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    if (activity != null) {
      AccountEditActionMode accountEditActionMode =
          new AccountEditActionMode(getComponent(), accountsAdapter);
      activity.startSupportActionMode(accountEditActionMode);
    }
  }

  @Override
  public void showBackupCreated(String backupFilePath) {
    String message = getContext().getString(R.string.backup_created_successfully, backupFilePath);
    Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void showEncryptionError() {
    Snackbar.make(coordinatorLayout, R.string.error_encrypting_backup, Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void showUnableToCreateBackupError() {
    Snackbar.make(coordinatorLayout, R.string.error_creating_backup_file, Snackbar.LENGTH_LONG).show();
  }

  private void showEmptyView() {
    rvAccounts.setVisibility(View.GONE);
    tvEmptyView.setVisibility(View.VISIBLE);
  }

  private void showAccountList(List<AccountPasscode> accounts) {
    if (accountsAdapter == null) {
      accountsAdapter = new AccountsAdapter(getContext(), accounts);
      accountsAdapter.editMode().subscribe(presenter::onEditModeList);
      rvAccounts.setAdapter(accountsAdapter);
    } else {
      accountsAdapter.setAccounts(accounts);
      accountsAdapter.notifyDataSetChanged();
    }

    rvAccounts.setVisibility(View.VISIBLE);
    tvEmptyView.setVisibility(View.GONE);
  }

  private void stopCounters() {
    if (accountsAdapter == null) {
      return;
    }

    for (int i = 0; i < accountsAdapter.getItemCount(); i++) {
      AccountViewHolder viewHolder =
          (AccountViewHolder) rvAccounts.findViewHolderForAdapterPosition(i);
      if (viewHolder != null) {
        viewHolder.stopAnimation();
      }
    }
  }
}
