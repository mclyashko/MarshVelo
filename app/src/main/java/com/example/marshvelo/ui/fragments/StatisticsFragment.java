package com.example.marshvelo.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;
import com.example.marshvelo.R;
import com.example.marshvelo.ui.viewmodels.MainViewModel;
import com.example.marshvelo.ui.viewmodels.StatisticsViewModel;

@AndroidEntryPoint
public class StatisticsFragment extends Fragment {

    private StatisticsViewModel viewModel;
    // TODO: Singleton
    public StatisticsFragment() {
        super(R.layout.fragment_statistics);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
    }
}
