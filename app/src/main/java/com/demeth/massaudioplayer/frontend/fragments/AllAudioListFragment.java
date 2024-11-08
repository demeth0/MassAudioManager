package com.demeth.massaudioplayer.frontend.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.demeth.massaudioplayer.R;
import com.demeth.massaudioplayer.backend.Dependencies;
import com.demeth.massaudioplayer.backend.Shiraori;
import com.demeth.massaudioplayer.backend.models.objects.Audio;
import com.demeth.massaudioplayer.databinding.FragmentHomeAudioEntryDisplayerBinding;
import com.demeth.massaudioplayer.frontend.HomeViewModel;
import com.demeth.massaudioplayer.frontend.service.AudioService;
import com.demeth.massaudioplayer.frontend.service.AudioServiceBoundable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AllAudioListFragment extends Fragment {
    @FunctionalInterface
    private interface SelectionListEdit{
        void edit(Set<Audio> list);
    }
    public static class AudioSelectionViewModel extends ViewModel {
        public static final int MULTI_SELECTION_DISABLED=0,MULTI_SELECTION_ENABLED=1;
        private final MutableLiveData<Integer> multi_selection_state = new MutableLiveData<>(MULTI_SELECTION_DISABLED);
        private final MutableLiveData<Set<Audio>> multi_selection_list = new MutableLiveData<>(new ArraySet<>());

        public void setMultiSelectionState(int state){
            multi_selection_state.postValue(state);
        }

        public LiveData<Integer> getMultiSelectionState(){
            return multi_selection_state;
        }

        @NonNull
        public LiveData<Set<Audio>>  getSelectionList(){
            return multi_selection_list;
        }

        public void editSelectionList(SelectionListEdit edit){
            Set<Audio> l = multi_selection_list.getValue();
            edit.edit(l);
            multi_selection_list.postValue(l);
        }
    }

    private class AudioRecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder>{
        Dependencies dep;
        public AudioRecyclerViewAdapter(Dependencies dep){
            this.dep = dep;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(FragmentHomeAudioEntryDisplayerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false), dep);
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

        private final CompoundButton.OnCheckedChangeListener checked_change_listener = (buttonView, isChecked) -> {
            selectionViewModel.editSelectionList(selection -> {
                if(isChecked){
                    selection.add(audio);
                } else {
                    selection.remove(audio);
                }
            });
        };

        public ViewHolder(FragmentHomeAudioEntryDisplayerBinding binding, Dependencies dep) {
            super(binding.getRoot());
            title = binding.title;
            checkBox = binding.checkBox;
            album = binding.album;

            binding.holderLayout.setOnLongClickListener(v -> {
                Log.d("whatever", "LogClick "+audio.toString());
                checkBox.setChecked(true);
                selectionViewModel.setMultiSelectionState(AudioSelectionViewModel.MULTI_SELECTION_ENABLED);

                return true;
            });
            binding.holderLayout.setOnClickListener(v -> {
                Log.d("wtf", "playing : "+audio.toString());
                Shiraori.playAudio(audio,dep);
            });

            checkBox.setOnCheckedChangeListener(checked_change_listener);

            viewModel.getCurrentAudioUI().observe(requireActivity(),audio1 -> {
                checkHighlighted();
            });

            selectionViewModel.getMultiSelectionState().observe(requireActivity(),integer -> {
                if(integer == AudioSelectionViewModel.MULTI_SELECTION_ENABLED)
                    checkBox.setVisibility(View.VISIBLE);
                else{
                    checkBox.setVisibility(View.GONE);
                    checkBox.setChecked(false);
                }

            });
        }

        private void checkHighlighted(){
            boolean highlighted = Objects.equals(audio, ref_to_current_audio);
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
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(Objects.requireNonNull(selectionViewModel.getSelectionList().getValue()).contains(audio));
            checkBox.setOnCheckedChangeListener(checked_change_listener);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_all_audio_list, container, false);
    }

    private HomeViewModel viewModel;

    private AudioSelectionViewModel selectionViewModel;

    private RecyclerView audio_list;
    private AudioRecyclerViewAdapter audio_list_adapter;

    private LinearLayoutCompat selection_layout;
    private TextView selection_counter;
    private CheckBox global_selection;
    private Button play_after;
    private ImageButton  play_all_random;

    private Audio ref_to_current_audio=null;

    private ArrayList<Audio> audio_list_data = new ArrayList<>(0);

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged", "DefaultLocale"})
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstance) {
        super.onViewCreated(view, savedInstance);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);
        selectionViewModel = new ViewModelProvider(requireActivity()).get(AudioSelectionViewModel.class);
        load_components(view);

        Bundle bun = this.getArguments();
        if (bun == null || !bun.containsKey("audio_service")) {
            return;
        }
        Dependencies dep = ((AudioService.ServiceBinder) Objects.requireNonNull(bun.getBinder("audio_service"))).getService((AudioServiceBoundable) requireActivity()).getDependencies();

        audio_list_adapter = new AudioRecyclerViewAdapter(dep);
        audio_list.setAdapter(audio_list_adapter);

        viewModel.getSearchQuery().observe(requireActivity(),s -> {
            audio_list_data = Shiraori.getDatabaseEntries(dep).stream().filter(a->a.display_name.toLowerCase().contains(s.toLowerCase())).sorted().collect(Collectors.toCollection(ArrayList::new));
            audio_list_adapter.notifyDataSetChanged();
        });

        viewModel.getCurrentAudioUI().observe(requireActivity(),audio -> {
            ref_to_current_audio = audio;
        });

        selectionViewModel.getMultiSelectionState().observe(requireActivity(),integer -> {
            if(integer == AudioSelectionViewModel.MULTI_SELECTION_ENABLED){
                selection_layout.setVisibility(View.VISIBLE);
            }else{
                selection_layout.setVisibility(View.GONE);
            }
        });
        selection_counter.setText(0+"");
        global_selection.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectionViewModel.editSelectionList(list -> {
                if(isChecked){
                    list.addAll(audio_list_data);
                }else{
                    list.clear();
                }
            });
            audio_list_adapter.notifyDataSetChanged();
        });

        selectionViewModel.getSelectionList().observe(requireActivity(),audio -> {
            selection_counter.setText(String.format("%d", audio.size()));
            if(audio.size() == audio_list_data.size())
                global_selection.setChecked(true);

            if(audio.isEmpty()){
                selectionViewModel.setMultiSelectionState(AudioSelectionViewModel.MULTI_SELECTION_DISABLED);
            }
        });

        play_after.setOnClickListener(v -> {
            Collection<Audio> sel = Objects.requireNonNull(selectionViewModel.getSelectionList().getValue()).stream().sorted().collect(Collectors.toCollection(ArrayList::new));
            Shiraori.playAudios(sel,dep);
            selectionViewModel.editSelectionList(Set::clear);
        });

        play_all_random.setOnClickListener(v -> {
            Shiraori.setRandomModeEnabled(true, dep);
            Shiraori.playInPlaylist(Shiraori.getDatabaseEntries(dep),dep);
        });
    }

    private void load_components(View view){
        audio_list = view.findViewById(R.id.audio_list);
        selection_layout = view.findViewById(R.id.selection_controls);
        selection_counter = view.findViewById(R.id.selection_counter);
        global_selection = view.findViewById(R.id.global_selection_checkbox);
        play_after = view.findViewById(R.id.selection_play_next);
        play_all_random = view.findViewById(R.id.play_all_random_button);
    }
}