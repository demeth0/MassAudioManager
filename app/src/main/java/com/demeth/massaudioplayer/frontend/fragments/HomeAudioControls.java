package com.demeth.massaudioplayer.frontend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.frontend.HomeViewModel;

public class HomeAudioControls extends Fragment {

    private HomeViewModel viewModel;

    public static HomeAudioControls newInstance() {
        return new HomeAudioControls();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_audio_controls, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance){
        super.onViewCreated(view, savedInstance);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
    }
}