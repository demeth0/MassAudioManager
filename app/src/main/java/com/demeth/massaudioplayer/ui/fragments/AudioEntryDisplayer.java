package com.demeth.massaudioplayer.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.models.adapters.AudioManager;
import com.demeth.massaudioplayer.backend.models.objects.Audio;


import com.demeth.massaudioplayer.service.AudioService;
import com.demeth.massaudioplayer.service.BoundableActivity;
import com.demeth.massaudioplayer.ui.AudioEntryRecyclerViewAdapter;
import com.demeth.massaudioplayer.ui.Category;
import com.demeth.massaudioplayer.ui.MainActivity;
import com.demeth.massaudioplayer.ui.SelectionManager;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;
import com.demeth.massaudioplayer.ui.viewmodel.ListViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class AudioEntryDisplayer extends Fragment {
    private ListViewModel viewModel=null;
    private DiffusionViewModel diffusionViewModel=null;
    private Category category=Category.PISTES;

    private final AudioEntryRecyclerViewAdapter rec_adapter = new AudioEntryRecyclerViewAdapter();
    private CheckBox select_all;
    private CompoundButton.OnCheckedChangeListener checked_change_listener;
    //for tied references
    private OnBackPressedCallback playlist_callback;
    private AudioEntryRecyclerViewAdapter.OnItemClicked playlist_onClickedPlaylist;
    // private Playlist active_playlist = null; //TODO playlists

    private View global_selection_layout;
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



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_entry_displayer_list, container, false);
        select_all = view.findViewById(R.id.main_global_selection_checkbox);
        global_selection_layout = view.findViewById(R.id.selection_layout);
        checked_change_listener = (compoundButton, b) -> {
            if(b){
                MainActivity.selection_manager.select(rec_adapter.getValues(),rec_adapter);
            }else{
                MainActivity.selection_manager.clear(rec_adapter);
            }
        };
        Bundle bun = this.getArguments();

        // Set the adapter
        if (diffusionViewModel!=null && viewModel != null) {
            Context context = view.getContext();
            RecyclerView recyclerView = view.findViewById(R.id.list);
            recyclerView.setLayoutManager(new GridLayoutManager(context, 1));


            //set before serviceDependentInitializations
            if(MainActivity.selection_manager.getSelected().isEmpty()){
                global_selection_layout.setVisibility(View.GONE);
            }else{
                global_selection_layout.setVisibility(View.VISIBLE);
            }

            if(bun!=null){
                if(bun.containsKey("category")){
                    category=  Category.valueOf(bun.getString("category"));
                }

                if(bun.containsKey("service")){
                    AudioService service = ((AudioService.AudioBinder) bun.getBinder("service")).getService((BoundableActivity) requireActivity());
                    serviceDependentInitializations(service,view);
                }
            }

            diffusionViewModel.getEntry().observe(getViewLifecycleOwner(), rec_adapter::setHighlighted);
            recyclerView.setAdapter(rec_adapter);

            //global selection

            TextView selection_counter = view.findViewById(R.id.selection_counter);



            MainActivity.selection_manager.setListener(new SelectionManager.SelectionListener() {
                @Override
                public void onSelectionEnabled() {
                    global_selection_layout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onSelectionDisabled() {
                    global_selection_layout.setVisibility(View.GONE);
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onItemSelected() {
                    updateCheckBox();
                    selection_counter.setText(Integer.toString(MainActivity.selection_manager.size()));
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onItemDeselected() {
                    updateCheckBox();
                    selection_counter.setText(Integer.toString(MainActivity.selection_manager.size()));
                }
            });

            select_all.setOnCheckedChangeListener(checked_change_listener);
        }
        return view;
    }

    private void serviceDependentInitializations(AudioService service, View view){
        rec_adapter.setOnItemClicked(entry -> {
            if(category.equals(Category.QUEUE)){
                service.play(entry);
            }else{
                service.set_playlist(Collections.singletonList(entry));
                service.play();
            }
        });

        //selection buttons
        Button selection_play = view.findViewById(R.id.selection_play);
        Button selection_add_to_queue = view.findViewById(R.id.selection_add_to_queue);
        Button selection_add_to_playlist = view.findViewById(R.id.selection_add_to_playlist);
        Button selection_play_after = view.findViewById(R.id.selection_play_after);

        selection_add_to_queue.setOnClickListener(view1 -> {
            List<Audio> new_list  = service.get_playlist();
            new_list.addAll(new ArrayList<>(MainActivity.selection_manager.getSelected()));
            // manager.set(new_list); // TODO do not call set to prevent shuffling
        });

        selection_play.setOnClickListener(view1 -> {
            service.set_playlist(new ArrayList<>(MainActivity.selection_manager.getSelected()));
            service.play();
        });

        selection_add_to_playlist.setOnClickListener(view1 -> {
            //TODO popup playlist
            Log.d("AudioEntryDisplayer","TODO a implémenter avec popup");
        });

        selection_play_after.setOnClickListener(view1 -> {
            List<Audio> new_list  = service.get_playlist();
            new_list.addAll(new_list.indexOf(service.get_current_audio()),MainActivity.selection_manager.getSelected());
            // manager.set(new_list); // TODO do not call set to prevent shuffling
        });

        //category list
        switch(category){
            case QUEUE:
                viewModel.getQueue().observe(getViewLifecycleOwner(), this::setRecAdapterContent);
                rec_adapter.setOnItemClicked(service::play);
                break;
            case PLAYLISTS:
                /*PlaylistManager manager = ((MainActivity)requireActivity()).getPlaylistManager();
                final Playlist.OnUpdateListener onUpdate = p -> setRecAdapterContent(p.get());
                global_selection_layout.setVisibility(View.GONE);
                playlist_onClickedPlaylist = entry -> {
                    active_playlist = manager.get(entry.getName());
                    setRecAdapterContent(active_playlist.get());
                    if(MainActivity.selection_manager.getSelected().isEmpty()){
                        global_selection_layout.setVisibility(View.GONE);
                    }else{
                        global_selection_layout.setVisibility(View.VISIBLE);
                    }
                    active_playlist.setOnUpdateListener(onUpdate);

                    //set return key to go back to menu
                    playlist_callback.setEnabled(true);
                    //play audio clicked
                    rec_adapter.setOnItemClicked(entry1 -> {
                        service.getPlayer().setPlaylist(active_playlist.get());
                        service.getPlayer().play();
                    });
                };

                playlist_callback = new OnBackPressedCallback(false) { // enabled by default
                    @Override
                    public void handleOnBackPressed() {
                        //return to playlists
                        setRecAdapterContent(manager.getAll());
                        rec_adapter.setOnItemClicked(playlist_onClickedPlaylist);
                        this.setEnabled(false);
                        global_selection_layout.setVisibility(View.GONE);
                        active_playlist.setOnUpdateListener(null);
                    }
                };

                requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),playlist_callback);
                setRecAdapterContent(manager.getAll());
                rec_adapter.setOnItemClicked(playlist_onClickedPlaylist);*/
                break;
            case PISTES:
                viewModel.getFilteredList().observe(getViewLifecycleOwner(), this::setRecAdapterContent);
                rec_adapter.setOnItemClicked(entry -> {
                    service.set_playlist(Collections.singletonList(entry));
                    service.play();
                });
                break;
        }
    }

    public Collection<Audio> getDisplayedContent(){
        return rec_adapter.getValues();
    }

    private void setRecAdapterContent(Collection<? extends Audio> data){
        rec_adapter.setContent(data);
        updateCheckBox();
    }

    private void updateCheckBox(){
        boolean future_state = rec_adapter.getValues().stream().parallel().allMatch(d -> MainActivity.selection_manager.getSelected().contains(d));

        if(future_state != select_all.isChecked()){
            select_all.setOnCheckedChangeListener(null);
            select_all.setChecked(
                    MainActivity.selection_manager.getSelected().containsAll(rec_adapter.getValues()));
            select_all.setOnCheckedChangeListener(checked_change_listener);
        }

    }
}