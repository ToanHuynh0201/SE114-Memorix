package com.example.memorix.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorix.data.remote.Repository.ShareRepository;
import com.example.memorix.data.remote.dto.Share.IncomingShare;

import java.util.List;

public class ShareViewModel extends ViewModel {
    private ShareRepository shareRepository;

    public ShareViewModel() {
        shareRepository = new ShareRepository();
    }

    // ==================== SHARE OPERATIONS ====================
    // Getters for share operations LiveData
    public LiveData<Boolean> getShareSuccess() {
        return shareRepository.getShareSuccess();
    }

    public LiveData<String> getShareError() {
        return shareRepository.getShareError();
    }

    public LiveData<Boolean> getShareLoading() {
        return shareRepository.getShareLoading();
    }

    /**
     * Share deck với user khác
     * @param deckId ID của deck cần share
     * @param receiverEmail Email của người nhận
     * @param permissionLevel Quyền hạn ("view", "edit", etc.)
     * @param token Auth token
     */
    public void shareDeck(long deckId, String receiverEmail, String permissionLevel, String token) {
        android.util.Log.d("ShareViewModel", "Sharing deck - ID: " + deckId + ", Email: " + receiverEmail);
        shareRepository.shareDeck(deckId, receiverEmail, permissionLevel, token);
    }

    // ==================== INCOMING SHARES ====================
    // Getters for incoming shares LiveData
    public LiveData<List<IncomingShare>> getIncomingShares() {
        return shareRepository.getIncomingShares();
    }

    public LiveData<String> getIncomingSharesError() {
        return shareRepository.getIncomingSharesError();
    }

    public LiveData<Boolean> getIncomingSharesLoading() {
        return shareRepository.getIncomingSharesLoading();
    }

    /**
     * Lấy danh sách các deck được share đến tài khoản của user
     * @param token Auth token
     */
    public void loadIncomingShares(String token) {
        android.util.Log.d("ShareViewModel", "Loading incoming shares");
        shareRepository.getIncomingShares(token);
    }

    /**
     * Refresh lại danh sách incoming shares
     * @param token Auth token
     */
    public void refreshIncomingShares(String token) {
        android.util.Log.d("ShareViewModel", "Refreshing incoming shares");
        shareRepository.resetIncomingSharesStates();
        shareRepository.getIncomingShares(token);
    }

    // ==================== STATE MANAGEMENT ====================
    /**
     * Reset share states - useful when starting a new share operation
     */
    public void resetShareStates() {
        android.util.Log.d("ShareViewModel", "Resetting share states");
        shareRepository.resetShareStates();
    }

    /**
     * Reset incoming shares states
     */
    public void resetIncomingSharesStates() {
        android.util.Log.d("ShareViewModel", "Resetting incoming shares states");
        shareRepository.resetIncomingSharesStates();
    }

    /**
     * Clear all states - for complete cleanup
     */
    public void clearAllStates() {
        android.util.Log.d("ShareViewModel", "Clearing all states");
        shareRepository.clearStates();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        android.util.Log.d("ShareViewModel", "ViewModel cleared");
        shareRepository.clearStates();
    }
}