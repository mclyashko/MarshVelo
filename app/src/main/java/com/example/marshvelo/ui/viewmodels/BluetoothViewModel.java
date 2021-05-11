package com.example.marshvelo.ui.viewmodels;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import com.example.marshvelo.repositories.MainRepository;

@HiltViewModel
public class BluetoothViewModel extends ViewModel {
    private MainRepository mainRepository;

    @Inject
    public BluetoothViewModel(MainRepository mainRepository)
    {
        this.mainRepository = mainRepository;
    }
}
