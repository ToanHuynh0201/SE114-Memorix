package com.example.memorix.ui.share;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.memorix.R;
import com.example.memorix.data.remote.dto.Share.IncomingShare;
import com.example.memorix.viewmodel.ShareViewModel;

import java.util.List;

public class InviteManagementFragment extends Fragment implements ReceivedInviteAdapter.OnInviteActionListener {

    // ViewModel và Adapter
    private ShareViewModel shareViewModel;
    private ReceivedInviteAdapter adapter;

    // Views
    private RecyclerView recyclerReceivedInvites;
    private LinearLayout layoutEmptyReceived;
    private ProgressBar progressLoading;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String authToken;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shareViewModel = new ViewModelProvider(this).get(ShareViewModel.class);
        authToken = getAuthToken();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invite_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        observeViewModel();
        loadIncomingShares();
    }

    private String getAuthToken() {
        if (authToken != null) {
            return authToken;
        }
        SharedPreferences prefs = requireActivity().getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        authToken = prefs.getString("access_token", null);
        return authToken;
    }

    private void initViews(View view) {
        recyclerReceivedInvites = view.findViewById(R.id.recycler_received_invites);
        layoutEmptyReceived = view.findViewById(R.id.layout_empty_received);
        progressLoading = view.findViewById(R.id.progress_loading);

        // Thêm SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        // Thêm refresh button trong empty state
        Button btnRefreshEmpty = view.findViewById(R.id.btn_refresh_empty);
        if (btnRefreshEmpty != null) {
            btnRefreshEmpty.setOnClickListener(v -> refreshData());
        }
    }

    private void setupRecyclerView() {

        adapter = new ReceivedInviteAdapter();
        adapter.setOnInviteActionListener(this);

        recyclerReceivedInvites.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerReceivedInvites.setAdapter(adapter);

    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::refreshData);

            // Set colors
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.primary_color,
                    R.color.accent_color
            );
        }
    }

    private void observeViewModel() {

        // Observe incoming shares
        shareViewModel.getIncomingShares().observe(getViewLifecycleOwner(), this::handleIncomingShares);

        // Observe loading state
        shareViewModel.getIncomingSharesLoading().observe(getViewLifecycleOwner(), this::handleLoadingState);

        // Observe errors
        shareViewModel.getIncomingSharesError().observe(getViewLifecycleOwner(), this::handleError);
    }

    private void handleIncomingShares(List<IncomingShare> shares) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }

        if (shares != null && !shares.isEmpty()) {
            adapter.setIncomingShares(shares);
            recyclerReceivedInvites.setVisibility(View.VISIBLE);
            layoutEmptyReceived.setVisibility(View.GONE);

        } else {
            recyclerReceivedInvites.setVisibility(View.GONE);
            layoutEmptyReceived.setVisibility(View.VISIBLE);

        }
    }

    private void handleLoadingState(Boolean isLoading) {
        if (isLoading != null) {
            if (isLoading) {
                progressLoading.setVisibility(View.VISIBLE);
                recyclerReceivedInvites.setVisibility(View.GONE);
                layoutEmptyReceived.setVisibility(View.GONE);
            } else {
                progressLoading.setVisibility(View.GONE);

                // Stop swipe refresh if running
                if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }

    private void handleError(String error) {
        if (error != null && !error.isEmpty()) {
            progressLoading.setVisibility(View.GONE);

            // Stop swipe refresh
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }

            // Show error message
            showToast(error);

            // Show empty state if no data
            if (adapter.getItemCount() == 0) {
                recyclerReceivedInvites.setVisibility(View.GONE);
                layoutEmptyReceived.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadIncomingShares() {
        if (authToken != null && !authToken.trim().isEmpty()) {
            shareViewModel.loadIncomingShares(authToken);
        } else {
            // Show empty state
            recyclerReceivedInvites.setVisibility(View.GONE);
            layoutEmptyReceived.setVisibility(View.VISIBLE);
        }
    }

    public void refreshData() {
        if (authToken != null && !authToken.trim().isEmpty()) {
            shareViewModel.refreshIncomingShares(authToken);
        } else {
            // Update token from preferences in case it was refreshed
            authToken = getAuthToken();
            if (authToken != null && !authToken.trim().isEmpty()) {
                shareViewModel.refreshIncomingShares(authToken);
            } else {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        }
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onAcceptInvite(IncomingShare share) {
        android.util.Log.d("InviteManagementFragment", "Accept invite for share ID: " + share.getShareId());

        // TODO: Implement accept invite API call
        showToast("Chấp nhận lời mời: " +
                (share.getDeckName() != null ? share.getDeckName() : "Unknown Deck"));

        // Refresh data after action
        refreshData();
    }

    @Override
    public void onDeclineInvite(IncomingShare share) {
        android.util.Log.d("InviteManagementFragment", "Decline invite for share ID: " + share.getShareId());

        // TODO: Implement decline invite API call
        showToast("Từ chối lời mời: " +
                (share.getDeckName() != null ? share.getDeckName() : "Unknown Deck"));

        // Refresh data after action
        refreshData();
    }

    // ==================== LIFECYCLE METHODS ====================
    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (shareViewModel != null) {
            shareViewModel.resetIncomingSharesStates();
        }
    }
}