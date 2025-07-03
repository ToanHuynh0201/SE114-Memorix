package com.example.memorix.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorix.data.remote.Repository.ShareRepository;
import com.example.memorix.data.remote.dto.Share.IncomingShare;
import com.example.memorix.data.remote.dto.Share.AcceptShareResponse;

import java.util.List;

public class ShareViewModel extends ViewModel {
    private final ShareRepository shareRepository;

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

    public void shareDeck(long deckId, String receiverEmail, String permissionLevel, String token) {
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
        shareRepository.getIncomingShares(token);
    }

    /**
     * Refresh lại danh sách incoming shares
     * @param token Auth token
     */
    public void refreshIncomingShares(String token) {
        shareRepository.resetIncomingSharesStates();
        shareRepository.getIncomingShares(token);
    }

    // ==================== ACCEPT SHARE OPERATIONS ====================
    // Getters for accept operations LiveData
    public LiveData<Boolean> getAcceptSuccess() {
        return shareRepository.getAcceptSuccess();
    }

    public LiveData<String> getAcceptError() {
        return shareRepository.getAcceptError();
    }

    public LiveData<Boolean> getAcceptLoading() {
        return shareRepository.getAcceptLoading();
    }

    public LiveData<AcceptShareResponse.ClonedDeck> getClonedDeck() {
        return shareRepository.getClonedDeck();
    }

    /**
     * Chấp nhận lời mời share deck
     * @param shareId ID của share
     * @param token Auth token
     */
    public void acceptShare(long shareId, String token) {
        shareRepository.acceptShare(shareId, token);
    }

    // ==================== DECLINE SHARE OPERATIONS ====================
    // Getters for decline operations LiveData
    public LiveData<Boolean> getDeclineSuccess() {
        return shareRepository.getDeclineSuccess();
    }

    public LiveData<String> getDeclineError() {
        return shareRepository.getDeclineError();
    }

    public LiveData<Boolean> getDeclineLoading() {
        return shareRepository.getDeclineLoading();
    }

    /**
     * Từ chối lời mời share deck
     * @param shareId ID của share
     * @param token Auth token
     */
    public void declineShare(long shareId, String token) {
        shareRepository.declineShare(shareId, token);
    }

    // ==================== STATE MANAGEMENT ====================
    /**
     * Reset share states - useful when starting a new share operation
     */
    public void resetShareStates() {
        shareRepository.resetShareStates();
    }

    /**
     * Reset incoming shares states
     */
    public void resetIncomingSharesStates() {
        shareRepository.resetIncomingSharesStates();
    }

    /**
     * Reset accept states
     */
    public void resetAcceptStates() {
        shareRepository.resetAcceptStates();
    }

    /**
     * Reset decline states
     */
    public void resetDeclineStates() {
        shareRepository.resetDeclineStates();
    }

    /**
     * Clear all states - for complete cleanup
     */
    public void clearAllStates() {
        shareRepository.clearStates();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        shareRepository.clearStates();
    }
}