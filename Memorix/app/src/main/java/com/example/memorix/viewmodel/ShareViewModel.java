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

    public LiveData<List<IncomingShare>> getIncomingShares() {
        return shareRepository.getIncomingShares();
    }

    public LiveData<String> getIncomingSharesError() {
        return shareRepository.getIncomingSharesError();
    }

    public LiveData<Boolean> getIncomingSharesLoading() {
        return shareRepository.getIncomingSharesLoading();
    }

    public void loadIncomingShares(String token) {
        shareRepository.getIncomingShares(token);
    }

    public void refreshIncomingShares(String token) {
        shareRepository.resetIncomingSharesStates();
        shareRepository.getIncomingShares(token);
    }

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

    public void acceptShare(long shareId, String token) {
        shareRepository.acceptShare(shareId, token);
    }

    public LiveData<Boolean> getDeclineSuccess() {
        return shareRepository.getDeclineSuccess();
    }

    public LiveData<String> getDeclineError() {
        return shareRepository.getDeclineError();
    }

    public LiveData<Boolean> getDeclineLoading() {
        return shareRepository.getDeclineLoading();
    }

    public void declineShare(long shareId, String token) {
        shareRepository.declineShare(shareId, token);
    }

    public void resetShareStates() {
        shareRepository.resetShareStates();
    }

    public void resetIncomingSharesStates() {
        shareRepository.resetIncomingSharesStates();
    }

    public void resetAcceptStates() {
        shareRepository.resetAcceptStates();
    }

    public void resetDeclineStates() {
        shareRepository.resetDeclineStates();
    }
    public void clearAllStates() {
        shareRepository.clearStates();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        shareRepository.clearStates();
    }
}