package com.demeth.massaudioplayer.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.placeholder.PlaceholderContent;
import com.demeth.massaudioplayer.service.AudioService;
import com.demeth.massaudioplayer.service.BoundableActivity;
import com.demeth.massaudioplayer.ui.AudioEntryRecyclerViewAdapter;
import com.demeth.massaudioplayer.ui.Category;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;
import com.demeth.massaudioplayer.ui.viewmodel.ListViewModel;

import java.util.Collections;

/**
 * A fragment representing a list of Items.
 */
public class AudioEntryDisplayer extends Fragment {
    private ListViewModel viewModel=null;
    private DiffusionViewModel diffusionViewModel=null;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AudioEntryDisplayer() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bun = this.getArguments();
        Log.d("AudioEntryDisplayer",bun==null?"null":bun.toString());
        if(bun!=null && bun.containsKey("service")) {
            AudioService service = ((AudioService.AudioBinder) bun.getBinder("service")).getService((BoundableActivity) requireActivity());
            viewModel = new ViewModelProvider(service).get(ListViewModel.class);
            diffusionViewModel = new ViewModelProvider(service).get(DiffusionViewModel.class);
        }
    }

    private Category category;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_entry_displayer_list, container, false);

        // Set the adapter
        if (diffusionViewModel!=null && viewModel != null) {
            Bundle bun = this.getArguments();


            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new GridLayoutManager(context, 1));

            AudioEntryRecyclerViewAdapter rec_adapter = new AudioEntryRecyclerViewAdapter();
            assert bun != null;

            if(bun.containsKey("service")) {
                AudioService service = ((AudioService.AudioBinder) bun.getBinder("service")).getService((BoundableActivity) requireActivity());
                rec_adapter.setOnItemClicked(entry -> {
                    if(category.equals(Category.QUEUE)){
                        service.getPlayer().moveToAudio(entry);
                    }else{
                        service.getPlayer().setPlaylist(Collections.singletonList(entry));
                        service.getPlayer().play();
                    }
                });
            }

            diffusionViewModel.getEntry().observe(getViewLifecycleOwner(), rec_adapter::setHighlighted);
            recyclerView.setAdapter(rec_adapter);

            if(bun.containsKey("category")){
                category=  Category.valueOf(bun.getString("category"));
                switch(category){
                    case QUEUE:
                        viewModel.getQueue().observe(getViewLifecycleOwner(), rec_adapter::setContent);
                        break;
                    case PLAYLISTS:
                        rec_adapter.setContent(Collections.EMPTY_LIST);
                        break;
                    case PISTES:
                        viewModel.getFilteredList().observe(getViewLifecycleOwner(), rec_adapter::setContent);
                        break;
                }
            }else{
                viewModel.getFilteredList().observe(getViewLifecycleOwner(), rec_adapter::setContent);
            }


        }
        return view;
    }
}