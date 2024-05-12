package com.demeth.massaudioplayer.frontend.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.Dependencies;
import com.demeth.massaudioplayer.backend.Shiraori;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.databinding.FragmentAudioEntryDisplayerBinding;
import com.demeth.massaudioplayer.frontend.HomeViewModel;
import com.demeth.massaudioplayer.frontend.service.AudioService;
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable;
import com.demeth.massaudioplayer.ui.AudioEntryRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class AllAudioListFragment extends Fragment {

    private class AudioRecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder>{
        Dependencies dep;
        public AudioRecyclerViewAdapter(Dependencies dep){
            this.dep = dep;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(FragmentAudioEntryDisplayerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), dep);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.setContent(audio_list_data.get((position)));
        }

        @Override
        public int getItemCount() {
            return audio_list_data.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final CheckBox checkBox;
        private final ImageButton album;
        public Audio audio;

        private boolean highlighted = false;

        public ViewHolder(FragmentAudioEntryDisplayerBinding binding, Dependencies dep) {
            super(binding.getRoot());
            title = binding.title;
            checkBox = binding.checkBox;
            album = binding.album;

            binding.holderLayout.setOnLongClickListener(v -> {
                Log.d("whatever", "LogClick "+audio.toString());
                return false;
            });
            binding.holderLayout.setOnClickListener(v -> {
                Shiraori.playAudio(audio,dep);
            });

            checkBox.setOnCheckedChangeListener((compoundButton, b) -> {
                // TODO selection list
                /*if(b) MainActivity.selection_manager.select(mItem,AudioEntryRecyclerViewAdapter.this);
                else MainActivity.selection_manager.unselect(mItem,AudioEntryRecyclerViewAdapter.this);*/
            });

            viewModel.getCurrentAudioUI().observe(requireActivity(),audio1 -> {
                checkHighlighted();
            });
        }

        private void checkHighlighted(){
            highlighted = Objects.equals(audio,ref_to_current_audio);
            if(highlighted){
                title.setTextColor(title.getContext().getColor(R.color.foreground)); //TODO add shadow
            }else{
                title.setTextColor(title.getContext().getColor(R.color.white));
            }
        }

        public void setContent(Audio new_entry) {
            audio = new_entry;

            //text box
            title.setText(audio.display_name);
            checkHighlighted();
            //album cover
            //album.setImageBitmap(AlbumLoader.getDefaultCover());
            //this.itemView.removeCallbacks(this);
            //this.itemView.postDelayed(this, 200);

            // check box
            //checkBox.setChecked(MainActivity.selection_manager.contains(mItem));
            /*make a thrust table to understand this (shoud hide when no element and show when elements*/
            /*if (mItem.type.equals(AudioType.PLAYLIST)) {
                checkBox.setVisibility(View.GONE);
            } else if ((MainActivity.selection_manager.size() > 0) != (checkBox.getVisibility() == View.VISIBLE)) {
                if (checkBox.getVisibility() == View.VISIBLE) {
                    checkBox.setVisibility(View.GONE);
                } else {
                    //TODO animate
                    checkBox.setVisibility(View.VISIBLE);
                }
            }*/
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_all_audio_list, container, false);
    }

    private HomeViewModel viewModel;
    private RecyclerView audio_list;
    private AudioRecyclerViewAdapter audio_list_adapter;

    private Audio ref_to_current_audio=null;

    private ArrayList<Audio> audio_list_data = new ArrayList<>(0);

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        load_components(view);

        Bundle bun = this.getArguments();
        if (bun == null || !bun.containsKey("audio_service")) {
            return;
        }
        Dependencies dep = ((AudioService.ServiceBinder) bun.getBinder("audio_service")).getService((AudioServiceBoundable) requireActivity()).getDependencies();

        audio_list_adapter = new AudioRecyclerViewAdapter(dep);
        audio_list.setAdapter(audio_list_adapter);

        viewModel.getSearchQuery().observe(requireActivity(),s -> {
            audio_list_data = Shiraori.getDatabaseEntries(dep).stream().filter(a->a.display_name.toLowerCase().contains(s.toLowerCase())).collect(Collectors.toCollection(ArrayList::new));
            // TODO refresh adapter
        });

        viewModel.getCurrentAudioUI().observe(requireActivity(),audio -> {
            ref_to_current_audio = audio;
        });
    }

    private void load_components(View view){
        audio_list = view.findViewById(R.id.audio_list);

    }
}