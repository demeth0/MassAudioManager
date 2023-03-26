package com.demeth.massaudioplayer.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.placeholder.PlaceholderContent;
import com.demeth.massaudioplayer.ui.AudioEntryRecyclerViewAdapter;
import com.demeth.massaudioplayer.ui.Category;
import com.demeth.massaudioplayer.ui.MainActivity;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;
import com.demeth.massaudioplayer.ui.viewmodel.ListViewModel;

/**
 * A fragment representing a list of Items.
 */
public class AudioEntryDisplayer extends Fragment {
    private ListViewModel viewModel;
    private DiffusionViewModel diffusionViewModel;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AudioEntryDisplayer() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
        diffusionViewModel = new ViewModelProvider((requireActivity())).get(DiffusionViewModel.class);
        Log.d("AudioEntryDisplayer",savedInstanceState==null?"null":savedInstanceState.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_entry_displayer_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            recyclerView.setLayoutManager(new GridLayoutManager(context, 1));

            AudioEntryRecyclerViewAdapter rec_adapter = new AudioEntryRecyclerViewAdapter();
            rec_adapter.setOnItemClicked(entry -> {
                diffusionViewModel.setEntry(entry); //TODO provisoir changer par control par service
            });

            diffusionViewModel.getEntry().observe(getViewLifecycleOwner(), rec_adapter::setHighlighted);

            recyclerView.setAdapter(rec_adapter);

            viewModel.getFilteredList().observe(getViewLifecycleOwner(), rec_adapter::setContent);

            viewModel.setList(PlaceholderContent.ITEMS); //TODO test list
        }
        return view;
    }
}