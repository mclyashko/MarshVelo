package com.example.marshvelo.ui.viewmodels;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

import com.example.marshvelo.database.Ride;
import com.example.marshvelo.repositories.MainRepository;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private MainRepository mainRepository;

    @Inject
    public MainViewModel(MainRepository mainRepository) {
        this.mainRepository = mainRepository;
    }

    public void insertRide(Ride ride) {
        mainRepository.insertRide(ride);
    }

}
