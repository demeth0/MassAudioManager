package com.demeth.massaudioplayer.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.database.IdentifiedEntry;
import com.demeth.massaudioplayer.database.playlist.Playlist;
import com.demeth.massaudioplayer.database.playlist.PlaylistManager;
import com.demeth.massaudioplayer.service.AudioService;
import com.demeth.massaudioplayer.service.BoundableActivity;
import com.demeth.massaudioplayer.ui.AudioEntryRecyclerViewAdapter;
import com.demeth.massaudioplayer.ui.Category;
import com.demeth.massaudioplayer.ui.MainActivity;
import com.demeth.massaudioplayer.ui.SelectionManager;
import com.demeth.massaudioplayer.ui.viewmodel.DiffusionViewModel;
import com.demeth.massaudioplayer.ui.viewmodel.ListViewModel;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

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
            View global_selection_layout = view.findViewById(R.id.selection_layout);
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

                @Override
                public void onItemSelected() {
                    updateCheckBox();
                    selection_counter.setText(Integer.toString(MainActivity.selection_manager.size()));
                }

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
                service.getPlayer().moveToAudio(entry);
            }else{
                service.getPlayer().setPlaylist(Collections.singletonList(entry));
                service.getPlayer().play();
            }
        });


        //selection buttons
        Button selection_play = view.findViewById(R.id.selection_play);
        Button selection_add_to_queue = view.findViewById(R.id.selection_add_to_queue);
        Button selection_add_to_playlist = view.findViewById(R.id.selection_add_to_playlist);

        selection_add_to_queue.setOnClickListener(view1 -> {
            Collection<IdentifiedEntry> new_queue = service.getPlayer().getPlaylist(false);
            new_queue.addAll(viewModel.getList().getValue().stream().parallel()
                    .filter(d->MainActivity.selection_manager.getSelected().contains(d.getId()))
                    .collect(Collectors.toSet()));

            service.getPlayer().setPlaylist(new_queue);
        });

        selection_play.setOnClickListener(view1 -> {
            service.getPlayer().setPlaylist(viewModel.getList().getValue().stream().parallel()
                    .filter(d->MainActivity.selection_manager.getSelected().contains(d.getId()))
                    .collect(Collectors.toSet()));
            service.getPlayer().play();
        });

        selection_add_to_playlist.setOnClickListener(view1 -> {
            //TODO popup playlist
            Log.d("AudioEntryDisplayer","TODO a implémenter avec popup");
        });

        //category list
        switch(category){
            case QUEUE:
                viewModel.getQueue().observe(getViewLifecycleOwner(), this::setRecAdapterContent);
                rec_adapter.setOnItemClicked(entry -> {
                    service.getPlayer().moveToAudio(entry);
                });
                break;
            case PLAYLISTS:
                PlaylistManager manager = ((MainActivity)requireActivity()).getPlaylistManager();

                playlist_onClickedPlaylist = entry -> {
                    Playlist p = manager.get(entry.getName());
                    setRecAdapterContent(p.get());

                    //set return key to go back to menu
                    playlist_callback.setEnabled(true);
                    //play audio clicked
                    rec_adapter.setOnItemClicked(entry1 -> {
                        service.getPlayer().setPlaylist(p.get());
                        service.getPlayer().play();
                    });
                };

                playlist_callback = new OnBackPressedCallback(false /* enabled by default */) {
                    @Override
                    public void handleOnBackPressed() {
                        //return to playlists
                        setRecAdapterContent(manager.getAll());
                        rec_adapter.setOnItemClicked(playlist_onClickedPlaylist);
                        this.setEnabled(false);
                    }
                };

                requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(),playlist_callback);
                setRecAdapterContent(manager.getAll());
                rec_adapter.setOnItemClicked(playlist_onClickedPlaylist);
                break;
            case PISTES:
                viewModel.getFilteredList().observe(getViewLifecycleOwner(), this::setRecAdapterContent);
                rec_adapter.setOnItemClicked(entry -> {
                    service.getPlayer().setPlaylist(Collections.singletonList(entry));
                    service.getPlayer().play();
                });
                break;
        }
    }

    private void setRecAdapterContent(Collection<? extends IdentifiedEntry> data){
        rec_adapter.setContent(data);
        updateCheckBox();
    }

    private void updateCheckBox(){
        boolean future_state = rec_adapter.getValues().stream().parallel().allMatch(d -> MainActivity.selection_manager.getSelected().contains(d.getId()));

        if(future_state != select_all.isChecked()){
            select_all.setOnCheckedChangeListener(null);
            select_all.setChecked(
                    rec_adapter.getValues().stream().allMatch(d -> MainActivity.selection_manager.getSelected().contains(d.getId())));
            select_all.setOnCheckedChangeListener(checked_change_listener);
        }

    }
}