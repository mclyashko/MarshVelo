package com.example.marshvelo.ui.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

import com.example.marshvelo.database.Ride;
import com.example.marshvelo.repositories.MainRepository;

import java.util.List;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {

    private MainRepository mainRepository;

    @Inject
    public StatisticsViewModel(MainRepository mainRepository) {
        this.mainRepository = mainRepository;
    }

    public LiveData<List<Ride>> ridesSortedByDate() {
        return mainRepository.getAllRidesSortedByDate();
    }
}
