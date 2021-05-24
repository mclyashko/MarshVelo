package com.example.marshvelo.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dagger.hilt.android.AndroidEntryPoint;
import com.example.marshvelo.R;
import com.example.marshvelo.adapters.RideAdapter;
import com.example.marshvelo.database.Ride;
import com.example.marshvelo.ui.viewmodels.MainViewModel;
import com.example.marshvelo.ui.viewmodels.StatisticsViewModel;

import java.util.List;

@AndroidEntryPoint
public class StatisticsFragment extends Fragment {

    private StatisticsViewModel viewModel;
    private RideAdapter rideAdapter;
    // TODO: Singleton
    public StatisticsFragment() {
        super(R.layout.fragment_statistics);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        setupRecyclerView();
        viewModel.ridesSortedByDate().observe(getViewLifecycleOwner(), new Observer<List<Ride>>() {
            @Override
            public void onChanged(List<Ride> rides) {
                rideAdapter.submitList(rides);
            }
        });

    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = getView().findViewById(R.id.recyclerview);
        rideAdapter = new RideAdapter(new RideAdapter.RideDiff());
        recyclerView.setAdapter(rideAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

    }
}
