package com.example.memorix.viewmodel;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.memorix.data.remote.Repository.FlashcardRepository;

public class EditCardViewModelFactory implements ViewModelProvider.Factory{
    private FlashcardRepository cardRepository;

    public EditCardViewModelFactory(FlashcardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EditCardViewModel.class)) {
            return (T) new EditCardViewModel(cardRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
