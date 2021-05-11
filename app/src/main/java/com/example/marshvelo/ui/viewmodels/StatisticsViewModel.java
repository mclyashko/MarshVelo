package com.example.marshvelo.ui.viewmodels;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import com.example.marshvelo.repositories.MainRepository;

@HiltViewModel
public class StatisticsViewModel extends ViewModel {

    private MainRepository mainRepository;

    @Inject
    public StatisticsViewModel(MainRepository mainRepository) {
        this.mainRepository = mainRepository;
    }
}
